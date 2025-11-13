import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Loader2, Plus, FileText, Search, Trash2 } from 'lucide-react';
import type { CollectionPaperDetailResponse } from '@/services/collection.service';
import type { UseMutationResult } from '@tanstack/react-query';

type SearchPaper = {
    id: string;
    title: string;
    authors?: string;
    journal?: string;
    publicationYear?: number | string;
};

type Props = {
    isLoadingPapers: boolean;
    papers: CollectionPaperDetailResponse[] | undefined;
    paginatedPapers: CollectionPaperDetailResponse[];
    papersPage: number;
    setPapersPage: (n: number) => void;
    totalPapersPages: number;
    canAddPaper: boolean;
    isAddPaperOpen: boolean;
    setIsAddPaperOpen: (b: boolean) => void;
    paperSearchQuery: string;
    setPaperSearchQuery: (s: string) => void;
    isLoadingPaperSearch: boolean;
    paginatedAvailablePapers: SearchPaper[];
    paperSearchDialogPage: number;
    setPaperSearchDialogPage: React.Dispatch<React.SetStateAction<number>>;
    totalDialogPages: number;
    addPaperMutation: UseMutationResult<unknown, unknown, unknown, unknown>;
    handleAddPaper: (id: string) => void;
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
    isAddPaperOpen,
    setIsAddPaperOpen,
    paperSearchQuery,
    setPaperSearchQuery,
    isLoadingPaperSearch,
    paginatedAvailablePapers,
    paperSearchDialogPage,
    setPaperSearchDialogPage,
    totalDialogPages,
    addPaperMutation,
    handleAddPaper,
    handleRemovePaper,
    handlePriorityClick,
    getStatusColor,
    getPriorityColor,
}) => {
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
                            <Card key={paper.paperId} className="hover:shadow-md transition-shadow">
                                <CardHeader>
                                    <div className="flex items-start justify-between">
                                        <div className="flex-1">
                                            <CardTitle className="text-lg mb-2">{paper.title}</CardTitle>
                                            <div className="flex flex-wrap gap-2 items-center text-sm text-muted-foreground">
                                                <span>{paper.authors}</span>
                                                {paper.journal && <span>• {paper.journal}</span>}
                                                {paper.publicationYear && <span>• {paper.publicationYear}</span>}
                                            </div>
                                        </div>
                                        <div className="flex gap-2">
                                            {paper.status && (
                                                <Badge className={`${getStatusColor(paper.status)} text-white`}>
                                                    {paper.status}
                                                </Badge>
                                            )}
                                            {paper.priority && (
                                                <Badge
                                                    variant="outline"
                                                    className={`${getPriorityColor(paper.priority)} text-white`}
                                                    onClick={() => handlePriorityClick(paper)}
                                                >
                                                    {paper.priority}
                                                </Badge>
                                            )}
                                            <Button variant="ghost" size="icon" onClick={() => handleRemovePaper(paper)}>
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </div>
                                </CardHeader>
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
                        <p className="text-muted-foreground mb-4">Add papers to this collection to get started</p>
                    </CardContent>
                </Card>
            )}

            {/* Add Paper Dialog */}
            <Dialog open={isAddPaperOpen} onOpenChange={(open) => {
                setIsAddPaperOpen(open);
                if (!open) {
                    setPaperSearchDialogPage(0);
                    setPaperSearchQuery('');
                }
            }}>
                <DialogContent className="max-w-3xl max-h-[80vh] overflow-hidden flex flex-col">
                    <DialogHeader>
                        <DialogTitle>Add Paper to Collection</DialogTitle>
                    </DialogHeader>
                    <div className="flex flex-col gap-4 flex-1 overflow-hidden">
                        <div className="relative">
                            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                            <Input
                                placeholder="Search papers by title, author, or keyword..."
                                value={paperSearchQuery}
                                onChange={(e) => {
                                    setPaperSearchQuery(e.target.value);
                                    setPaperSearchDialogPage(0);
                                }}
                                className="pl-10"
                            />
                        </div>

                        <div className="flex-1 overflow-y-auto space-y-2">
                            {isLoadingPaperSearch ? (
                                <div className="flex justify-center items-center py-12">
                                    <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
                                </div>
                            ) : paginatedAvailablePapers && paginatedAvailablePapers.length > 0 ? (
                                <>
                                    {paginatedAvailablePapers.map((paper) => (
                                        <Card key={paper.id} className="hover:shadow-md transition-shadow">
                                            <CardHeader>
                                                <CardTitle className="text-base line-clamp-2">{paper.title}</CardTitle>
                                            </CardHeader>
                                            <CardContent>
                                                <Button
                                                    onClick={() => handleAddPaper(paper.id)}
                                                    disabled={addPaperMutation.isPending}
                                                    size="sm"
                                                    className="w-full"
                                                >
                                                    {addPaperMutation.isPending ? (
                                                        <>
                                                            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                                            Adding...
                                                        </>
                                                    ) : (
                                                        <>
                                                            <Plus className="h-4 w-4 mr-2" />
                                                            Add to Collection
                                                        </>
                                                    )}
                                                </Button>
                                            </CardContent>
                                        </Card>
                                    ))}

                                    <div className="flex items-center justify-center gap-3 pt-4 border-t">
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => setPaperSearchDialogPage(Math.max(0, paperSearchDialogPage - 1))}
                                            disabled={paperSearchDialogPage === 0 || isLoadingPaperSearch || totalDialogPages <= 1}
                                        >
                                            Previous
                                        </Button>
                                        <span className="text-sm text-muted-foreground min-w-[140px] text-center">
                                            Page {paperSearchDialogPage + 1} of {totalDialogPages}
                                        </span>
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => setPaperSearchDialogPage(p => Math.min(totalDialogPages - 1, p + 1))}
                                            disabled={paperSearchDialogPage >= totalDialogPages - 1 || isLoadingPaperSearch || totalDialogPages <= 1}
                                        >
                                            Next
                                        </Button>
                                    </div>
                                </>
                            ) : (
                                <div className="text-center py-12">
                                    <FileText className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                                    <h3 className="text-lg font-semibold mb-2">No papers found</h3>
                                    <p className="text-muted-foreground">
                                        {paperSearchQuery ? 'Try a different search query' : 'Start typing to search for papers'}
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                </DialogContent>
            </Dialog>
        </>
    );
};

export default PapersTab;
