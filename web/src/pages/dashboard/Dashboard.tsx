import { useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Plus, BookOpen, Star } from "lucide-react";
import PaperCard from "@/components/PaperCard";
import { Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { toast } from "sonner";
import { Helmet } from "react-helmet-async";
import { getPaginatedPapers, importPaper, getFavoritePapers } from "@/services/paper.service.ts";
import Header from "@/components/Header";
import DashboardHeader from "./components/DashboardHeader";
import SearchAndFilter from "./components/SearchAndFilter";
import { CreatePaperRequest } from "@/types/paper.types";
import { getWorkflowsByUser } from "@/services/progress.service";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { supabase } from "@/integrations/supabase/client";


const Dashboard = () => {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const [isImportOpen, setIsImportOpen] = useState(false);
    const [activeTab, setActiveTab] = useState<'all' | 'favourites'>('all');
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

    // Query for all papers (default tab)
    const { data, isLoading, error } = useQuery({
        queryKey: ['papers', user?.id, searchQuery, page, filters, activeTab],
        queryFn: async () => {
            if (activeTab === 'favourites') {
                // Fetch favorites from backend API
                const response = await getFavoritePapers(user!.id);
                let favorites = response?.data || [];

                // Apply search filter if provided
                if (searchQuery) {
                    const searchLower = searchQuery.toLowerCase();
                    favorites = favorites.filter((paper: any) =>
                        paper.title?.toLowerCase().includes(searchLower) ||
                        paper.authors?.toLowerCase().includes(searchLower) ||
                        paper.journal?.toLowerCase().includes(searchLower)
                    );
                }

                // Apply filters
                if (filters.author) {
                    favorites = favorites.filter((paper: any) =>
                        paper.authors?.toLowerCase().includes(filters.author.toLowerCase())
                    );
                }
                if (filters.journal) {
                    favorites = favorites.filter((paper: any) =>
                        paper.journal?.toLowerCase().includes(filters.journal.toLowerCase())
                    );
                }
                if (filters.yearFrom) {
                    favorites = favorites.filter((paper: any) =>
                        paper.publicationYear >= parseInt(filters.yearFrom)
                    );
                }
                if (filters.yearTo) {
                    favorites = favorites.filter((paper: any) =>
                        paper.publicationYear <= parseInt(filters.yearTo)
                    );
                }

                // Client-side pagination
                const startIndex = (page - 1) * pageSize;
                const endIndex = startIndex + pageSize;
                const paginatedPapers = favorites.slice(startIndex, endIndex);

                return {
                    status: 200,
                    data: {
                        papers: paginatedPapers,
                        totalElements: favorites.length,
                        totalPages: Math.max(1, Math.ceil(favorites.length / pageSize)),
                    },
                };
            } else {
                // Use existing API for 'all' tab
                return await getPaginatedPapers(page, pageSize, searchQuery, filters, user?.id);
            }
        },
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
        let processedPapers = papers.map(paper => {
            // Helper function to decode base64 encoded ID
            const decodeId = (encodedId: string): string => {
                try {
                    if (/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(encodedId)) {
                        return encodedId;
                    }
                    return atob(encodedId);
                } catch (e) {
                    return encodedId;
                }
            };

            // Try to find workflow by matching decoded paperId
            let workflow = null;
            if (workflows) {
                workflow = workflows.find(w => {
                    const decodedWorkflowPaperId = decodeId(w.paperId);
                    return decodedWorkflowPaperId === paper.id || w.paperId === paper.id;
                });
            }

            // If not found in workflows array, try workflowMap (might have different key format)
            if (!workflow) {
                workflow = workflowMap.get(paper.id);
                // Also try with encoded paper.id
                if (!workflow) {
                    const encodedPaperId = btoa(paper.id);
                    workflow = workflowMap.get(encodedPaperId);
                }
            }

            // Use workflow data if available, otherwise use paper data
            let last_read_page = paper.last_read_page;
            let total_pages = paper.total_pages || null;
            let progress = paper.progress || null;

            if (workflow) {
                // Prefer workflow data
                last_read_page = workflow.lastPage || paper.last_read_page || null;
                progress = workflow.progress || paper.progress || null;

                // Calculate total_pages from progress if available
                if (workflow.progress > 0 && workflow.lastPage > 0) {
                    total_pages = Math.ceil((workflow.lastPage / (workflow.progress / 100)));
                } else if (!total_pages && last_read_page && last_read_page > 0) {
                    // If we have last_read_page but no progress, estimate total_pages
                    total_pages = null; // Keep null if we can't calculate accurately
                }
            }
            return {
                ...paper,
                last_read_page: last_read_page,
                total_pages: total_pages,
                progress: progress,
            };
        });

        return processedPapers;
    }, [papers, workflowMap, activeTab, workflows]);

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

                        {/* Tabs */}
                        <Tabs value={activeTab} onValueChange={(v) => {
                            setActiveTab(v as 'all' | 'favourites');
                            setPage(1); // Reset to first page when switching tabs
                        }}>
                            <TabsList className="grid w-full grid-cols-2">
                                <TabsTrigger value="all">All Papers</TabsTrigger>
                                <TabsTrigger value="favourites">
                                    <Star className="h-4 w-4 mr-2" />
                                    Favourites
                                </TabsTrigger>
                            </TabsList>
                        </Tabs>

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
                                <h3 className="text-lg font-semibold mb-2">
                                    {activeTab === 'favourites' 
                                        ? 'No favourite papers yet'
                                        : 'No papers yet'}
                                </h3>
                                <p className="text-muted-foreground mb-4">
                                    {searchQuery 
                                        ? 'No papers found matching your search' 
                                        : activeTab === 'favourites'
                                        ? 'Mark papers as favourites to see them here'
                                        : 'Import your first paper to get started'}
                                </p>
                                {!searchQuery && activeTab === 'all' && (
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
