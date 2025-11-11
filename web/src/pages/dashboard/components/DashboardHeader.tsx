import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Plus } from "lucide-react";

interface DashboardHeaderProps {
    isImportOpen: boolean;
    setIsImportOpen: (open: boolean) => void;
    newPaper: {
        title: string;
        authors: string;
        journal: string;
        publicationYear?: number;
        doi: string;
        description: string;
    };
    setNewPaper: React.Dispatch<React.SetStateAction<{
        title: string;
        authors: string;
        journal: string;
        publicationYear?: number | undefined;
        doi: string;
        description: string;
    }>>;
    importMutation: {
        mutate: () => void;
        isPending: boolean;
    };
}

const DashboardHeader = ({
    isImportOpen,
    setIsImportOpen,
    newPaper,
    setNewPaper,
    importMutation
}: DashboardHeaderProps) => {
    return (
        <>
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold mb-2">My Library</h1>
                    <p className="text-muted-foreground">
                        Manage and organize your research papers
                    </p>
                </div>

                <Dialog open={isImportOpen} onOpenChange={setIsImportOpen}>
                    <DialogTrigger asChild>
                        <Button size="lg" className="sm:w-auto w-full">
                            <Plus className="h-5 w-5 mr-2" />
                            Import Paper
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                        <DialogHeader>
                            <DialogTitle>Import New Paper</DialogTitle>
                        </DialogHeader>
                        <div className="space-y-4">
                            <div className="space-y-2">
                                <Label htmlFor="title">Title *</Label>
                                <Input
                                    id="title"
                                    value={newPaper.title}
                                    onChange={(e) => setNewPaper({ ...newPaper, title: e.target.value })}
                                    placeholder="Paper title"
                                    required
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="authors">Authors * (comma separated)</Label>
                                <Input
                                    id="authors"
                                    value={newPaper.authors}
                                    onChange={(e) => setNewPaper({ ...newPaper, authors: e.target.value })}
                                    placeholder="John Doe, Jane Smith"
                                    required
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label htmlFor="journal">Journal</Label>
                                    <Input
                                        id="journal"
                                        value={newPaper.journal}
                                        onChange={(e) => setNewPaper({
                                            ...newPaper,
                                            journal: e.target.value
                                        })}
                                        placeholder="Journal name"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="year">Year</Label>
                                    <Input
                                        id="year"
                                        type="number"
                                        value={newPaper.publicationYear}
                                        onChange={(e) => setNewPaper({
                                            ...newPaper,
                                            publicationYear: parseInt(e.target.value)
                                        })}
                                        placeholder="2024"
                                    />
                                </div>
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="doi">DOI</Label>
                                <Input
                                    id="doi"
                                    value={newPaper.doi}
                                    onChange={(e) => setNewPaper({ ...newPaper, doi: e.target.value })}
                                    placeholder="10.1234/example"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label htmlFor="description">Description</Label>
                                <Textarea
                                    id="description"
                                    value={newPaper.description}
                                    onChange={(e) => setNewPaper({ ...newPaper, description: e.target.value })}
                                    placeholder="Paper description..."
                                    rows={5}
                                />
                            </div>

                            <Button
                                onClick={() => importMutation.mutate()}
                                disabled={!newPaper.title || !newPaper.authors || importMutation.isPending}
                                className="w-full"
                            >
                                {importMutation.isPending ? 'Importing...' : 'Import Paper'}
                            </Button>
                        </div>
                    </DialogContent>
                </Dialog>
            </div>
        </>
    );
};

export default DashboardHeader;