import { useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Plus, BookOpen, Star, Clock } from "lucide-react";
import PaperCard from "@/components/PaperCard";
import RecentlyReadCard from "./components/RecentlyReadCard";
import { Link } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";
import { toast } from "sonner";
import { Helmet } from "react-helmet-async";
import { getPaginatedPapers, importPaper, getFavoritePapers, getPaperDetails } from "@/services/paper.service.ts";
import Header from "@/components/Header";
import DashboardHeader from "./components/DashboardHeader";
import SearchAndFilter from "./components/SearchAndFilter";
import { CreatePaperRequest } from "@/types/paper.types";
import { getWorkflowsByUser } from "@/services/progress.service";

import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";


const Dashboard = () => {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const [isImportOpen, setIsImportOpen] = useState(false);
    const [activeTab, setActiveTab] = useState<'all' | 'recently_read' | 'favourites'>('all');
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
            } else if (activeTab === 'recently_read') {
                try {
                    // Fetch from backend API - get all workflows, then filter for active reading
                    const encodedUserId = btoa(user!.id);
                    let allWorkflows;
                    try {
                        allWorkflows = await getWorkflowsByUser(encodedUserId);
                        console.log('Recently read: Fetched', allWorkflows?.length || 0, 'workflows for user');
                    } catch (error) {
                        console.error('Failed to fetch workflows:', error);
                        return {
                            status: 200,
                            data: {
                                papers: [],
                                totalElements: 0,
                                totalPages: 1,
                            },
                        };
                    }

                    // Filter workflows that are actively being read
                    // Show all workflows except "unread" with no progress and no lastPage
                    const activeWorkflows = (allWorkflows || []).filter(workflow => {
                        if (!workflow || !workflow.paperId) {
                            console.log('Recently read: Skipping workflow without paperId:', workflow);
                            return false;
                        }
                        
                        const hasProgress = workflow.progress !== null && workflow.progress !== undefined 
                            && workflow.progress > 0;
                        const hasLastPage = workflow.lastPage !== null && workflow.lastPage !== undefined 
                            && workflow.lastPage > 0;
                        const isReading = workflow.status === 'reading';
                        const isFinished = workflow.status === 'finished';
                        const isUnread = workflow.status === 'unread';
                        
                        // Exclude only if: status is "unread" AND no progress AND no lastPage
                        // Include everything else (reading, finished, or unread with progress/lastPage)
                        const shouldExclude = isUnread && !hasProgress && !hasLastPage;
                        
                        if (!shouldExclude) {
                            console.log('Recently read: Including workflow:', {
                                paperId: workflow.paperId,
                                status: workflow.status,
                                progress: workflow.progress,
                                lastPage: workflow.lastPage
                            });
                        } else {
                            console.log('Recently read: Excluding unread workflow with no progress:', {
                                paperId: workflow.paperId,
                                status: workflow.status
                            });
                        }
                        
                        return !shouldExclude;
                    });

                    console.log('Recently read: Filtered', activeWorkflows.length, 'active workflows from', allWorkflows?.length || 0, 'total workflows');

                    if (!activeWorkflows || activeWorkflows.length === 0) {
                        console.log('Recently read: No active workflows found');
                        return {
                            status: 200,
                            data: {
                                papers: [],
                                totalElements: 0,
                                totalPages: 1,
                            },
                        };
                    }

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

                    // Get paper details for each workflow
                    const paperPromises = activeWorkflows.map(async (workflow) => {
                        try {
                            if (!workflow || !workflow.paperId) {
                                console.log('Recently read: Skipping workflow without paperId');
                                return null;
                            }

                            // Backend expects encoded ID in query param and will decode it
                            // Try multiple ID formats to handle different encoding scenarios
                            const decodedPaperId = decodeId(workflow.paperId);
                            const isUUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(workflow.paperId);
                            
                            console.log('Recently read: Fetching paper details for workflow:', {
                                paperId: workflow.paperId,
                                decodedPaperId: decodedPaperId,
                                isUUID: isUUID,
                                progress: workflow.progress,
                                lastPage: workflow.lastPage,
                                status: workflow.status
                            });

                            let paperResponse = null;
                            let lastError = null;
                            let workingId = null;
                            
                            // Try different ID formats
                            const idVariants = [
                                workflow.paperId, // Original (likely encoded)
                                decodedPaperId,   // Decoded
                                isUUID ? workflow.paperId : btoa(workflow.paperId), // Re-encode if not UUID
                            ];
                            
                            // Remove duplicates
                            const uniqueIds = [...new Set(idVariants)];
                            
                            for (const paperIdToTry of uniqueIds) {
                                try {
                                    console.log('Recently read: Trying paper ID:', paperIdToTry);
                                    paperResponse = await getPaperDetails(paperIdToTry, user!.id);
                                    if (paperResponse?.status === 200 && paperResponse?.data) {
                                        console.log('Recently read: Success with ID:', paperIdToTry);
                                        workingId = paperIdToTry;
                                        break;
                                    }
                                } catch (e: any) {
                                    console.log('Recently read: Failed with ID', paperIdToTry, ':', e.message);
                                    lastError = e;
                                    continue;
                                }
                            }
                            
                            if (!paperResponse || paperResponse?.status !== 200 || !paperResponse?.data) {
                                console.error('Recently read: All ID variants failed. Last error:', lastError);
                                console.log('Recently read: Paper response:', paperResponse);
                                return null;
                            }

                            // Successfully fetched paper details
                            const paper = paperResponse.data;
                            
                            // Determine ID for URL navigation
                            // Backend expects encoded ID in URL params (it will decode it)
                            // So we need to ensure the ID in URL is encoded
                            let paperIdForUrl: string;
                            
                            // If working ID is a UUID (decoded), encode it for URL
                            if (workingId && /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(workingId)) {
                                // Working ID is UUID, encode it
                                paperIdForUrl = btoa(workingId);
                            } else if (workingId) {
                                // Working ID is already encoded or in correct format
                                paperIdForUrl = workingId;
                            } else {
                                // Fallback: use workflow.paperId (should be encoded)
                                paperIdForUrl = workflow.paperId;
                            }
                            
                            const paperData = {
                                id: paperIdForUrl, // Use encoded ID for URL navigation
                                title: paper.title || '',
                                authors: Array.isArray(paper.authors) 
                                    ? paper.authors.join(', ') 
                                    : (typeof paper.authors === 'string' ? paper.authors : ''),
                                journal: paper.journal || '',
                                publicationYear: paper.publicationYear || paper.year || null,
                                doi: paper.doi || '',
                                description: paper.description || paper.abstract || '',
                                keywords: Array.isArray(paper.keywords) ? paper.keywords : [],
                                status: workflow.status || null,
                                priority: null,
                                isFavorite: false,
                                last_read_page: workflow.lastPage || 0,
                                total_pages: paper.totalPages || paper.total_pages || null,
                                progress: workflow.progress || null,
                            };
                            console.log('Recently read: Successfully fetched paper:', paperData.title, 'with ID:', paperData.id);
                            return paperData;
                        } catch (error) {
                            console.error('Recently read: Failed to fetch paper', workflow?.paperId, ':', error);
                            return null;
                        }
                    });

                    const papersWithDetails = (await Promise.all(paperPromises)).filter(Boolean);
                    console.log('Recently read: Found', papersWithDetails.length, 'papers with details from', activeWorkflows.length, 'workflows');

                    // Apply search filter if provided
                    let filteredPapers = papersWithDetails;
                    if (searchQuery) {
                        const searchLower = searchQuery.toLowerCase();
                        filteredPapers = filteredPapers.filter((paper: any) =>
                            paper.title?.toLowerCase().includes(searchLower) ||
                            paper.authors?.toLowerCase().includes(searchLower) ||
                            paper.journal?.toLowerCase().includes(searchLower)
                        );
                    }

                    // Apply filters
                    if (filters.author) {
                        filteredPapers = filteredPapers.filter((paper: any) =>
                            paper.authors?.toLowerCase().includes(filters.author.toLowerCase())
                        );
                    }
                    if (filters.journal) {
                        filteredPapers = filteredPapers.filter((paper: any) =>
                            paper.journal?.toLowerCase().includes(filters.journal.toLowerCase())
                        );
                    }
                    if (filters.yearFrom) {
                        filteredPapers = filteredPapers.filter((paper: any) =>
                            paper.publicationYear >= parseInt(filters.yearFrom)
                        );
                    }
                    if (filters.yearTo) {
                        filteredPapers = filteredPapers.filter((paper: any) =>
                            paper.publicationYear <= parseInt(filters.yearTo)
                        );
                    }

                    // Client-side pagination
                    const startIndex = (page - 1) * pageSize;
                    const endIndex = startIndex + pageSize;
                    const paginatedPapers = filteredPapers.slice(startIndex, endIndex);

                    return {
                        status: 200,
                        data: {
                            papers: paginatedPapers,
                            totalElements: filteredPapers.length,
                            totalPages: Math.max(1, Math.ceil(filteredPapers.length / pageSize)),
                        },
                    };
                } catch (error) {
                    console.error('Error in recently_read tab:', error);
                    return {
                        status: 200,
                        data: {
                            papers: [],
                            totalElements: 0,
                            totalPages: 1,
                        },
                    };
                }
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
            // Encode userId (base64) as required by backend API
            const encodedUserId = btoa(user.id);
            return await getWorkflowsByUser(encodedUserId);
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

    // Enhance papers with workflow data (only for 'all' and 'favourites' tabs)
    // For 'recently_read' tab, papers already have progress data from backend
    const papersWithProgress = useMemo(() => {
        // For recently_read tab, papers already have progress data from backend query
        if (activeTab === 'recently_read') {
            return papers;
        }

        // For other tabs, enhance papers with workflow data
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
                            setActiveTab(v as 'all' | 'recently_read' | 'favourites');
                            setPage(1); // Reset to first page when switching tabs
                        }}>
                            <TabsList className="grid w-full grid-cols-3">
                                <TabsTrigger value="all">All Papers</TabsTrigger>
                                <TabsTrigger value="recently_read">
                                    <Clock className="h-4 w-4 mr-2" />
                                    Recently Read
                                </TabsTrigger>
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
                                {papersWithProgress.map((paper) => {
                                    // Use RecentlyReadCard for recently_read tab, otherwise use PaperCard
                                    if (activeTab === 'recently_read') {
                                        return (
                                            <RecentlyReadCard
                                                key={paper.id}
                                                id={paper.id}
                                                title={paper.title}
                                                authors={paper.authors}
                                                journal={paper.journal}
                                                publicationYear={paper.publicationYear}
                                                last_read_page={paper.last_read_page}
                                                total_pages={paper.total_pages}
                                                progress={paper.progress}
                                            />
                                        );
                                    } else {
                                        return (
                                            <Link key={paper.id} to={`/paper/${paper.id}`}>
                                                <PaperCard {...paper} />
                                            </Link>
                                        );
                                    }
                                })}
                            </div>
                        ) : (
                            <div className="text-center py-12">
                                <BookOpen className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                                <h3 className="text-lg font-semibold mb-2">
                                    {activeTab === 'favourites' 
                                        ? 'No favourite papers yet' 
                                        : activeTab === 'recently_read'
                                        ? 'No recently read papers'
                                        : 'No papers yet'}
                                </h3>
                                <p className="text-muted-foreground mb-4">
                                    {searchQuery 
                                        ? 'No papers found matching your search' 
                                        : activeTab === 'favourites'
                                        ? 'Mark papers as favourites to see them here'
                                        : activeTab === 'recently_read'
                                        ? 'Papers you\'ve read will appear here'
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
