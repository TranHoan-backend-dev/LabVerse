import { useState, useEffect, useRef } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { ChevronLeft, ChevronRight, ZoomIn, ZoomOut, Download } from 'lucide-react';
import 'react-pdf/dist/Page/AnnotationLayer.css';
import 'react-pdf/dist/Page/TextLayer.css';

// Configure PDF.js worker - use local worker from public folder
pdfjs.GlobalWorkerOptions.workerSrc = '/pdf.worker.min.mjs';

// Memoize PDF options to prevent unnecessary reloads
const pdfOptions = {
  cMapUrl: `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjs.version}/cmaps/`,
  cMapPacked: true,
};

interface PDFViewerProps {
  pdfUrl: string;
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  paperId?: string;
  onProgressUpdate?: (pageNumber: number, totalPages: number) => void;
}

const PDFViewer = ({ pdfUrl, isOpen, onClose, title = 'PDF Viewer', paperId, onProgressUpdate }: PDFViewerProps) => {
  const [numPages, setNumPages] = useState<number>(0);
  const [pageNumber, setPageNumber] = useState<number>(1);
  const [scale, setScale] = useState<number>(1.0);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const progressUpdateTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Debounced progress update to avoid too many API calls
  const updateProgress = (page: number, total: number) => {
    if (progressUpdateTimeoutRef.current) {
      clearTimeout(progressUpdateTimeoutRef.current);
    }
    progressUpdateTimeoutRef.current = setTimeout(() => {
      if (onProgressUpdate && paperId) {
        onProgressUpdate(page, total);
      }
    }, 1000); // Wait 1 second before updating
  };

  const onDocumentLoadSuccess = ({ numPages }: { numPages: number }) => {
    setNumPages(numPages);
    setLoading(false);
    setError(null);
    // Notify parent about total pages (debounced)
    if (numPages > 0) {
      updateProgress(1, numPages);
    }
  };

  const onDocumentLoadError = (error: Error) => {
    console.error('Error loading PDF:', error);
    setError('Failed to load PDF. Please try again.');
    setLoading(false);
  };

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (progressUpdateTimeoutRef.current) {
        clearTimeout(progressUpdateTimeoutRef.current);
      }
    };
  }, []);

  const goToPrevPage = () => {
    setPageNumber((prev) => {
      const newPage = Math.max(1, prev - 1);
      // Update progress when page changes (debounced)
      if (numPages > 0) {
        updateProgress(newPage, numPages);
      }
      return newPage;
    });
  };

  const goToNextPage = () => {
    setPageNumber((prev) => {
      const newPage = Math.min(numPages, prev + 1);
      // Update progress when page changes (debounced)
      if (numPages > 0) {
        updateProgress(newPage, numPages);
      }
      return newPage;
    });
  };

  const zoomIn = () => {
    setScale((prev) => Math.min(3.0, prev + 0.2));
  };

  const zoomOut = () => {
    setScale((prev) => Math.max(0.5, prev - 0.2));
  };

  const handleDownload = () => {
    window.open(pdfUrl, '_blank');
  };

  const resetViewer = () => {
    setPageNumber(1);
    setScale(1.0);
    setLoading(true);
    setError(null);
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => {
      if (!open) {
        resetViewer();
        onClose();
      }
    }}>
      <DialogContent className="max-w-[95vw] w-full h-[95vh] p-0 flex flex-col">
        <DialogHeader className="px-6 pt-6 pb-4 border-b">
          <div className="flex items-center justify-between">
            <div>
              <DialogTitle>{title}</DialogTitle>
              <DialogDescription className="sr-only">
                PDF viewer with navigation and zoom controls
              </DialogDescription>
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={handleDownload}
                className="gap-2"
              >
                <Download className="h-4 w-4" />
                Download
              </Button>
            </div>
          </div>
        </DialogHeader>

        {/* Controls */}
        <div className="px-6 py-3 border-b flex items-center justify-between gap-4 bg-muted/50">
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={goToPrevPage}
              disabled={pageNumber <= 1}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <span className="text-sm font-medium min-w-[100px] text-center">
              Page {pageNumber} of {numPages || '--'}
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={goToNextPage}
              disabled={pageNumber >= numPages}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={zoomOut}
              disabled={scale <= 0.5}
            >
              <ZoomOut className="h-4 w-4" />
            </Button>
            <span className="text-sm font-medium min-w-[60px] text-center">
              {Math.round(scale * 100)}%
            </span>
            <Button
              variant="outline"
              size="sm"
              onClick={zoomIn}
              disabled={scale >= 3.0}
            >
              <ZoomIn className="h-4 w-4" />
            </Button>
          </div>
        </div>

        {/* PDF Content */}
        <div className="flex-1 overflow-auto bg-gray-100 p-4 flex justify-center">
          {error ? (
            <div className="flex items-center justify-center h-full">
              <div className="text-center">
                <p className="text-destructive mb-2">{error}</p>
                <Button onClick={() => window.location.reload()}>Retry</Button>
              </div>
            </div>
          ) : (
            <div className="bg-white shadow-lg">
              <Document
                file={pdfUrl}
                onLoadSuccess={onDocumentLoadSuccess}
                onLoadError={onDocumentLoadError}
                loading={
                  <div className="flex items-center justify-center p-8">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                    <span className="ml-3 text-sm text-muted-foreground">Loading PDF...</span>
                  </div>
                }
                error={
                  <div className="p-8 text-center">
                    <p className="text-destructive mb-2">Failed to load PDF</p>
                    <p className="text-sm text-muted-foreground">The PDF may be unavailable or there may be a network error.</p>
                  </div>
                }
                options={pdfOptions}
              >
                <Page
                  pageNumber={pageNumber}
                  scale={scale}
                  renderTextLayer={true}
                  renderAnnotationLayer={true}
                  className="border"
                />
              </Document>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default PDFViewer;

