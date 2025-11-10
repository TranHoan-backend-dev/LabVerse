import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Search, Plus, Filter, BookOpen } from "lucide-react";
import PaperCard from "@/components/PaperCard";
import { Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
// import {toast} from "sonner";
import { Helmet } from "react-helmet-async";
import { getPaginatedPapers } from "@/services/paper.service.ts";
import Header from "@/pages/Header.tsx";

const Dashboard = () => {
    const { user, signOut } = useAuth();
    const queryClient = useQueryClient();
    const [isImportOpen, setIsImportOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState('');
    const [newPaper, setNewPaper] = useState({
        authors: '',
        dataUrl: '',
        description: '',
        doi: '',
        id: '',
        journal: '',
        keywords: [],
        publicationYear: new Date().getFullYear(),
        title: '',
    });
    const [page, setPage] = useState(1);
    const pageSize = 12;
    const [filters, setFilters] = useState({
        author: '',
        journal: '',
        yearFrom: '',
        yearTo: '',
    });

    const { data, isLoading } = useQuery({
        queryKey: ['papers', user?.id, searchQuery, page],
        queryFn: async () => await getPaginatedPapers(page, pageSize, searchQuery),
        enabled: !!user,
    });

    const papers = data?.data.content ?? [];
    const total = data?.data.totalElements ?? 0;
    const totalPages = data?.data.pageable.pageSize ?? Math.max(1, Math.ceil(total / pageSize));

    const importMutation = useMutation({
        // TODO: xu ly import paper
        // mutationFn: async () => {
        //     const authorsArray = newPaper.authors
        //         .split(',')
        //         .map(a => a.trim())
        //         .filter(a => a);
        //
        //     const payload = {
        //         title: newPaper.title,
        //         authors: authorsArray,
        //         journal: newPaper.journal || null,
        //         year: newPaper.year || null,
        //         abstract: newPaper.abstract || null,
        //         doi: newPaper.doi || null,
        //     };
        //
        //     const res = await
        //
        //     if (!res.ok) throw new Error('Failed to import paper');
        // },
        // onSuccess: () => {
        //     toast.success('Paper imported successfully');
        //     queryClient.invalidateQueries({queryKey: ['papers']});
        //     setIsImportOpen(false);
        //     setNewPaper({
        //         title: '',
        //         authors: '',
        //         journal: '',
        //         year: new Date().getFullYear(),
        //         abstract: '',
        //         doi: '',
        //     });
        // },
        // onError: () => {
        //     toast.error('Failed to import paper');
        // },
    });

    return (
        <>
            <Helmet>
                <title>My Library – LabVerse</title>
                <meta name="description" content="Manage and organize your research papers in LabVerse." />
                <meta property="og:title" content="My Library – LabVerse" />
                <meta property="og:description" content="Manage and organize your research papers easily." />
                <meta property="og:type" content="website" />
            </Helmet>
            <div className="min-h-screen bg-background">
                <Header />

                {/* Main Content */}
                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="space-y-8">
                        {/* Page Header */}
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
                                            <Label htmlFor="abstract">Abstract</Label>
                                            <Textarea
                                                id="abstract"
                                                value={newPaper.description}
                                                onChange={(e) => setNewPaper({ ...newPaper, description: e.target.value })}
                                                placeholder="Paper abstract..."
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

                        {/* Search and Filters */}
                        <div className="rounded-xl border border-border bg-card p-6 shadow-custom-sm">
                            <div className="flex flex-col sm:flex-row gap-4">
                                <div className="relative flex-1">
                                    <Search
                                        className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                                    <Input
                                        placeholder="Search papers by title or author..."
                                        className="pl-10"
                                        value={searchQuery}
                                        onChange={(e) => setSearchQuery(e.target.value)}
                                    />
                                </div>
                                <Dialog>
                                    <DialogTrigger asChild>
                                        <Button variant="outline">
                                            <Filter className="h-5 w-5 mr-2" />
                                            Filters
                                        </Button>
                                    </DialogTrigger>

                                    <DialogContent className="max-w-md">
                                        <DialogHeader>
                                            <DialogTitle>Filters</DialogTitle>
                                        </DialogHeader>

                                        <div className="space-y-4">
                                            <div className="space-y-2">
                                                <Label>Author</Label>
                                                <Input
                                                    value={filters.author}
                                                    onChange={(e) => setFilters({ ...filters, author: e.target.value })}
                                                    placeholder="John Doe"
                                                />
                                            </div>

                                            <div className="space-y-2">
                                                <Label>Journal</Label>
                                                <Input
                                                    value={filters.journal}
                                                    onChange={(e) => setFilters({ ...filters, journal: e.target.value })}
                                                    placeholder="Nature, Science..."
                                                />
                                            </div>

                                            <div className="grid grid-cols-2 gap-4">
                                                <div className="space-y-2">
                                                    <Label>Year From</Label>
                                                    <Input
                                                        type="number"
                                                        value={filters.yearFrom}
                                                        onChange={(e) => setFilters({
                                                            ...filters,
                                                            yearFrom: e.target.value
                                                        })}
                                                        placeholder="1990"
                                                    />
                                                </div>

                                                <div className="space-y-2">
                                                    <Label>Year To</Label>
                                                    <Input
                                                        type="number"
                                                        value={filters.yearTo}
                                                        onChange={(e) => setFilters({
                                                            ...filters,
                                                            yearTo: e.target.value
                                                        })}
                                                        placeholder="2024"
                                                    />
                                                </div>
                                            </div>

                                            <Button
                                                className="w-full"
                                                onClick={() => setPage(1)}
                                            >
                                                Apply Filters
                                            </Button>
                                        </div>
                                    </DialogContent>
                                </Dialog>
                            </div>
                        </div>

                        {/* Papers Grid */}
                        {isLoading ? (
                            <div className="flex justify-center py-12">
                                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                            </div>
                        ) : papers && papers.length > 0 ? (
                            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                                {papers.map((paper) => (
                                    <Link key={paper.id} to={`/paper/${paper.id}`}>
                                        <PaperCard {...paper} />
                                    </Link>
                                ))}
                            </div>
                        ) : (
                            <div className="text-center py-12">
                                <BookOpen className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                                <h3 className="text-lg font-semibold mb-2">No papers yet</h3>
                                <p className="text-muted-foreground mb-4">
                                    {searchQuery ? 'No papers found matching your search' : 'Import your first paper to get started'}
                                </p>
                                {!searchQuery && (
                                    <Button onClick={() => setIsImportOpen(true)}>
                                        <Plus className="h-4 w-4 mr-2" />
                                        Import Paper
                                    </Button>
                                )}
                            </div>
                        )}
                        {totalPages > 1 && (
                            <div className="flex justify-center items-center gap-3 mt-8">
                                <Button
                                    variant="outline"
                                    disabled={page === 1}
                                    onClick={() => setPage(page - 1)}
                                >
                                    Previous
                                </Button>

                                <span className="text-sm font-medium">Page {page} / {totalPages}</span>

                                <Button
                                    variant="outline"
                                    disabled={page === totalPages}
                                    onClick={() => setPage(page + 1)}
                                >
                                    Next
                                </Button>
                            </div>
                        )}
                    </div>
                </main>
            </div>
        </>
    );
};

export default Dashboard;
