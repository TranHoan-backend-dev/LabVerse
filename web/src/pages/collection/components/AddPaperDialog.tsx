import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Search, Plus, Loader2, FileText } from 'lucide-react';
import type { UseMutationResult } from '@tanstack/react-query';

type SearchPaper = {
    id: string;
    title: string;
    authors?: string;
    journal?: string;
    publicationYear?: number | string;
};

export type AddPaperDialogProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    paperSearchQuery: string;
    setPaperSearchQuery: (s: string) => void;
    isLoadingPaperSearch: boolean;
    paginatedAvailablePapers: SearchPaper[];
    paperSearchDialogPage: number;
    setPaperSearchDialogPage: React.Dispatch<React.SetStateAction<number>>;
    totalDialogPages: number;
    addPaperMutation: UseMutationResult<unknown, unknown, unknown, unknown>;
    handleAddPaper: (id: string) => void;
};

type Props = AddPaperDialogProps;

const AddPaperDialog: React.FC<Props> = ({
    open,
    onOpenChange,
    paperSearchQuery,
    setPaperSearchQuery,
    isLoadingPaperSearch,
    paginatedAvailablePapers,
    paperSearchDialogPage,
    setPaperSearchDialogPage,
    totalDialogPages,
    addPaperMutation,
    handleAddPaper,
}) => {
    return (
        <Dialog
            open={open}
            onOpenChange={(open) => {
                onOpenChange(open);
                if (!open) {
                    setPaperSearchQuery('');
                    setPaperSearchDialogPage(0);
                }
            }}
        >
            <DialogTrigger asChild>
                <Button>
                    <Plus className="h-4 w-4 mr-2" />
                    Add Paper
                </Button>
            </DialogTrigger>
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
                                            <div className="text-sm text-muted-foreground space-y-1">
                                                <p className="line-clamp-1">{paper.authors}</p>
                                                {paper.journal && <p>{paper.journal}</p>}
                                                {paper.publicationYear && <p>{paper.publicationYear}</p>}
                                            </div>
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
                                {paginatedAvailablePapers && paginatedAvailablePapers.length > 0 && (
                                    <div className="flex items-center justify-center gap-3 pt-4 border-t">
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => setPaperSearchDialogPage((p) => Math.max(0, p - 1))}
                                            disabled={paperSearchDialogPage === 0 || isLoadingPaperSearch || totalDialogPages <= 1}
                                        >
                                            Previous
                                        </Button>
                                        <span className="text-sm text-muted-foreground min-w-[140px] text-center">
                                            Page {paperSearchDialogPage + 1} of {totalDialogPages}
                                            {paginatedAvailablePapers.length > 0 && (
                                                <span className="block text-xs mt-1">
                                                    ({paginatedAvailablePapers.length} total papers)
                                                </span>
                                            )}
                                        </span>
                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() => setPaperSearchDialogPage((p) => Math.min(totalDialogPages - 1, p + 1))}
                                            disabled={paperSearchDialogPage >= totalDialogPages - 1 || isLoadingPaperSearch || totalDialogPages <= 1}
                                        >
                                            Next
                                        </Button>
                                    </div>
                                )}
                            </>
                        ) : (
                            <div className="text-center py-12">
                                <FileText className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                                <h3 className="text-lg font-semibold mb-2">No papers found</h3>
                                <p className="text-muted-foreground">
                                    {paperSearchQuery
                                        ? 'Try a different search query'
                                        : 'Start typing to search for papers'}
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default AddPaperDialog;

