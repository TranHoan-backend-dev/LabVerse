import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { Loader2, Plus, FileText, Trash2 } from 'lucide-react';
import type { CollectionPaperDetailResponse } from '@/services/collection.service';
import type { UseMutationResult } from '@tanstack/react-query';

type SearchPaper = {
    id: string;
    title: string;
    authors?: string;
    journal?: string;
    publicationYear?: number | string;
};

type PaperWithProgress = CollectionPaperDetailResponse & {
    last_read_page?: number | null;
    total_pages?: number | null;
    progress?: number | null;
};

type Props = {
    isLoadingPapers: boolean;
    papers: PaperWithProgress[] | undefined;
    paginatedPapers: PaperWithProgress[];
    papersPage: number;
    setPapersPage: (n: number) => void;
    totalPapersPages: number;
    canAddPaper: boolean;
    canSetPriority: boolean;
    handleRemovePaper: (p: CollectionPaperDetailResponse) => void;
    handlePriorityClick: (p: CollectionPaperDetailResponse) => void;
    getStatusColor: (s?: string) => string;
    getPriorityColor: (p?: string) => string;
};

const PapersTab: React.FC<Props> = ({
    isLoadingPapers,
    papers,
    paginatedPapers,
    papersPage,
    setPapersPage,
    totalPapersPages,
    canAddPaper,
    canSetPriority,
    handleRemovePaper,
    handlePriorityClick,
    getStatusColor,
    getPriorityColor,
}) => {
    const navigate = useNavigate();
    return (
        <>
            {isLoadingPapers ? (
                <div className="flex justify-center py-12">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                </div>
            ) : papers && papers.length > 0 ? (
                <>
                    <div className="grid gap-4">
                        {paginatedPapers.map((paper) => (
                            <Card 
                                key={paper.paperId} 
                                className="hover:shadow-md transition-shadow"
                            >
                                <CardHeader>
                                    <div className="flex items-start justify-between">
                                        <div 
                                            className="flex-1 cursor-pointer"
                                            onClick={() => navigate(`/paper/${paper.paperId}`)}
                                            role="button"
                                            tabIndex={0}
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    e.preventDefault();
                                                    navigate(`/paper/${paper.paperId}`);
                                                }
                                            }}
                                        >
                                            <CardTitle className="text-lg mb-2">{paper.title}</CardTitle>
                                            <div className="flex flex-wrap gap-2 items-center text-sm text-muted-foreground">
                                                <span>{paper.authors}</span>
                                                {paper.journal && <span>• {paper.journal}</span>}
                                                {paper.publicationYear && <span>• {paper.publicationYear}</span>}
                                            </div>
                                        </div>
                                        <div className="flex gap-2" onClick={(e) => e.stopPropagation()} onKeyDown={(e) => e.stopPropagation()}>
                                            {paper.status && (
                                                <Badge 
                                                    className={`${getStatusColor(paper.status)} text-white`}
                                                    title="Status is calculated automatically based on all members' reading progress"
                                                >
                                                    {paper.status}
                                                </Badge>
                                            )}
                                            {paper.priority && (
                                                <Badge
                                                    variant="outline"
                                                    className={`${getPriorityColor(paper.priority)} text-white ${canSetPriority ? 'cursor-pointer hover:opacity-80' : ''}`}
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        if (canSetPriority) handlePriorityClick(paper);
                                                    }}
                                                    title={canSetPriority ? 'Click to change priority' : 'Only collection authors can change priority'}
                                                >
                                                    {paper.priority}
                                                </Badge>
                                            )}
                                            <Button 
                                                variant="ghost" 
                                                size="icon" 
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleRemovePaper(paper);
                                                }}
                                            >
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                </CardHeader>
                                {/* Reading Progress Bar */}
                                {paper.total_pages && paper.last_read_page !== null && paper.last_read_page !== undefined && (
                                    <CardContent className="pt-0 pb-4">
                                        <div className="space-y-1.5">
                                            <div className="flex items-center justify-between text-xs text-muted-foreground">
                                                <span>Reading Progress</span>
                                                <span>
                                                    {paper.last_read_page} / {paper.total_pages} pages ({Math.min(100, Math.round((paper.last_read_page / paper.total_pages) * 100))}%)
                                                </span>
                                            </div>
                                            <Progress 
                                                value={Math.min(100, Math.round((paper.last_read_page / paper.total_pages) * 100))} 
                                                className="h-2" 
                                            />
                                        </div>
                                    </CardContent>
                                )}
                            </Card>
                        ))}
                    </div>
                    {papers && papers.length > 0 && (
                        <div className="flex items-center justify-center gap-3 mt-6 pt-4 border-t">
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setPapersPage(Math.max(0, papersPage - 1))}
                                disabled={papersPage === 0 || totalPapersPages <= 1}
                            >
                                Previous
                            </Button>
                            <span className="text-sm text-muted-foreground min-w-[120px] text-center">
                                Page {papersPage + 1} of {totalPapersPages}
                                <span className="block text-xs mt-1">({papers.length} total papers)</span>
                            </span>
                            <Button
                                variant="outline"
                                size="sm"
                                onClick={() => setPapersPage(Math.min(totalPapersPages - 1, papersPage + 1))}
                                disabled={papersPage >= totalPapersPages - 1 || totalPapersPages <= 1}
                            >
                                Next
                            </Button>
                        </div>
                    )}
                </>
            ) : (
                <Card className="text-center py-12">
                    <CardContent>
                        <FileText className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                        <h3 className="text-lg font-semibold mb-2">No papers yet</h3>
                        <p className="text-muted-foreground mb-4">
                            {canAddPaper
                                ? 'Add papers to this collection to get started'
                                : 'No papers have been added to this collection'}
                        </p>
                    </CardContent>
                </Card>
            )}
        </>
    );
};

export default PapersTab;
