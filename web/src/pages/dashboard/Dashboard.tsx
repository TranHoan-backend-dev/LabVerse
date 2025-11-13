import { useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Plus, BookOpen } from "lucide-react";
import PaperCard from "@/components/PaperCard";
import { Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { toast } from "sonner";
import { Helmet } from "react-helmet-async";
import { getPaginatedPapers, importPaper } from "@/services/paper.service.ts";
import Header from "@/pages/Header.tsx";
import DashboardHeader from "./components/DashboardHeader";
import SearchAndFilter from "./components/SearchAndFilter";
import { CreatePaperRequest } from "@/types/paper.type";
import { getWorkflowsByUser } from "@/services/progress.service";

const Dashboard = () => {
    const { user } = useAuth();
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
        file: new File([], ''),
    });
    const [page, setPage] = useState(1);
    const pageSize = 12;
    const [filters, setFilters] = useState({
        author: '',
        journal: '',
        yearFrom: '',
        yearTo: '',
    });

    const { data, isLoading, error } = useQuery({
        queryKey: ['papers', user?.id, searchQuery, page, filters],
        queryFn: async () => await getPaginatedPapers(page, pageSize, searchQuery, filters),
        enabled: !!user,
        retry: 1,
    });

    const papers = data?.data?.papers ?? [];
    const total = data?.data?.totalElements ?? 0;
    const totalPages = data?.data?.totalPages ?? Math.max(1, Math.ceil(total / pageSize));

    // Get workflows for current user to show progress
    const { data: workflows } = useQuery({
        queryKey: ['workflows', user?.id],
        queryFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await getWorkflowsByUser(user.id);
        },
        enabled: !!user,
    });

    // Create a map of paperId -> workflow for quick lookup
    const workflowMap = useMemo(() => {
        if (!workflows) return new Map();
        const map = new Map();
        workflows.forEach(workflow => {
            map.set(workflow.paperId, workflow);
        });
        return map;
    }, [workflows]);

    // Enhance papers with workflow data
    const papersWithProgress = useMemo(() => {
        return papers.map(paper => {
            const workflow = workflowMap.get(paper.id);
            // Calculate total_pages from progress if available
            let total_pages = null;
            if (workflow && workflow.progress > 0 && workflow.lastPage > 0) {
                total_pages = Math.ceil((workflow.lastPage / (workflow.progress / 100)));
            }
            return {
                ...paper,
                last_read_page: workflow?.lastPage ?? null,
                total_pages: total_pages,
            };
        });
    }, [papers, workflowMap]);

    const importMutation = useMutation({
        mutationFn: async () => {
            console.log(newPaper.file.size == 0)
            if (newPaper.file.size == 0) throw new Error('No file provided. Please select a PDF file to import.');

            const payload: CreatePaperRequest = {
                file: newPaper.file,
                title: newPaper.title,
                authors: newPaper.authors,
                journal: newPaper.journal || null,
                publicationYear: newPaper.publicationYear || null,
                doi: newPaper.doi || null,
                description: newPaper.description || null,
                userId: user!.id,
            };

            const res = await importPaper(payload);

            if (res.status !== 200) throw new Error('Failed to import paper');
        },
        onSuccess: () => {
            toast.success('Paper imported successfully');
            queryClient.invalidateQueries({ queryKey: ['papers'] });
            setIsImportOpen(false);
            setNewPaper({
                authors: '',
                dataUrl: '',
                description: '',
                doi: '',
                id: '',
                journal: '',
                keywords: [],
                publicationYear: new Date().getFullYear(),
                title: '',
                file: new File([], ''),
            });
        },
        onError: (e: Error) => {
            toast.error(e.message);
        },
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
                        <DashboardHeader
                            isImportOpen={isImportOpen}
                            setIsImportOpen={setIsImportOpen}
                            newPaper={newPaper}
                            setNewPaper={setNewPaper}
                            importMutation={importMutation}
                        />

                        {/* Search and Filters */}
                        <SearchAndFilter
                            searchQuery={searchQuery}
                            setSearchQuery={setSearchQuery}
                            filters={filters}
                            setFilters={setFilters}
                        />

                        {/* Papers Grid */}
                        {isLoading ? (
                            <div className="flex justify-center py-12">
                                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                            </div>
                        ) : error ? (
                            <div className="text-center py-12">
                                <BookOpen className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                                <h3 className="text-lg font-semibold mb-2">Error loading papers</h3>
                                <p className="text-muted-foreground mb-4">
                                    {error instanceof Error ? error.message : 'Failed to load papers. Please try again.'}
                                </p>
                                <Button onClick={() => window.location.reload()}>
                                    Retry
                                </Button>
                            </div>
                        ) : papers && papers.length > 0 ? (
                            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                                {papersWithProgress.map((paper) => (
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
                        {totalPages && (
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
