import {useParams, useNavigate} from "react-router-dom";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {useState, useMemo, useEffect} from "react";
import {Button} from "@/components/ui/button";
import {Card, CardContent} from "@/components/ui/card";
import {FileText, Users, BarChart3, Plus} from "lucide-react";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import Header from "@/pages/Header.tsx";
import {
    getCollectionById,
    getPapersInCollection,
    updatePaperStatus,
    removePaperFromCollection,
    addPaperToCollection,
    getCollectionMembers,
    addMemberToCollectionByEmail,
    removeMemberFromCollection,
    type CollectionPaperDetailResponse,
    type CollectionUserResponse
} from "@/services/collection.service";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {getPaginatedPapers} from "@/services/paper.service.ts";
import ProgressDashboard from "@/pages/collection/components/ProgressDashboard";
import {getWorkflowsByUser} from "@/services/progress.service";
import CollectionHeader from "@/pages/collection/components/CollectionHeader";
import AddPaperDialog from "@/pages/collection/components/AddPaperDialog";
import AddMemberDialog from "@/pages/collection/components/AddMemberDialog";
import PapersTab from "@/pages/collection/components/PapersTab";
import MembersTab from "@/pages/collection/components/MembersTab";
import PriorityDialog from "@/pages/collection/components/PriorityDialog";

const CollectionDetails = () => {
    const {id} = useParams<{id: string}>();
    const {user} = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [isAddPaperOpen, setIsAddPaperOpen] = useState(false);
    const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
    const [isPriorityDialogOpen, setIsPriorityDialogOpen] = useState(false);
    const [selectedPaper, setSelectedPaper] = useState<CollectionPaperDetailResponse | null>(null);
    const [selectedPriority, setSelectedPriority] = useState<string>('MEDIUM');
    const [newPaperId, setNewPaperId] = useState('');
    const [newMemberEmail, setNewMemberEmail] = useState('');
    const [newMemberAccessLevel, setNewMemberAccessLevel] = useState<'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR'>('CONTRIBUTOR');
    const [activeTab, setActiveTab] = useState<'papers' | 'members' | 'progress'>('papers');
    
    // Papers list pagination
    const [papersPage, setPapersPage] = useState(0);
    const papersPageSize = 10;
    
    // Paper search states
    const [paperSearchQuery, setPaperSearchQuery] = useState('');
    const [paperSearchDialogPage, setPaperSearchDialogPage] = useState(0); // Client-side pagination for dialog
    const paperSearchDialogPageSize = 10; // 10 papers per page in dialog
    const paperSearchApiPageSize = 100; // Maximum allowed by backend API

    const {data: collection, isLoading: isLoadingCollection} = useQuery({
        queryKey: ['collection', id],
        queryFn: async () => {
            if (!id) throw new Error('Collection ID is required');
            return await getCollectionById(id);
        },
        enabled: !!id && !!user,
    });

    const {data: papers, isLoading: isLoadingPapers} = useQuery({
        queryKey: ['collection-papers', id],
        queryFn: async () => {
            if (!id) throw new Error('Collection ID is required');
            return await getPapersInCollection(id);
        },
        enabled: !!id && !!user && activeTab === 'papers',
    });

    // Load members immediately to check user permissions (not just when on members tab)
    const {data: members, isLoading: isLoadingMembers} = useQuery({
        queryKey: ['collection-members', id],
        queryFn: async () => {
            if (!id) throw new Error('Collection ID is required');
            return await getCollectionMembers(id);
        },
        enabled: !!id && !!user,
    });

    // Get workflows for current user to show reading progress
    const {data: workflows} = useQuery({
        queryKey: ['workflows', user?.id, id],
        queryFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await getWorkflowsByUser(user.id);
        },
        enabled: !!user && !!id,
    });

    // Create a map of paperId -> workflow for quick lookup (filtered by collectionId)
    const workflowMap = useMemo(() => {
        if (!workflows || !id) return new Map();
        const map = new Map();
        workflows.forEach(workflow => {
            // Only include workflows for this collection
            if (workflow.collectionId === id) {
                map.set(workflow.paperId, workflow);
            }
        });
        return map;
    }, [workflows, id]);

    // Enhance papers with workflow data (progress and total pages)
    const papersWithProgress = useMemo(() => {
        if (!papers) return [];
        return papers.map(paper => {
            const workflow = workflowMap.get(paper.paperId);
            // Calculate total_pages from progress if available
            let total_pages = null;
            if (workflow && workflow.progress > 0 && workflow.lastPage > 0) {
                total_pages = Math.ceil((workflow.lastPage / (workflow.progress / 100)));
            }
            return {
                ...paper,
                last_read_page: workflow?.lastPage ?? null,
                total_pages: total_pages,
                progress: workflow?.progress ?? null,
            };
        });
    }, [papers, workflowMap]);

    // Determine user's access level from members list or collection response
    const currentUserMember = useMemo(() => {
        if (!members || !user?.id) return null;
        // memberId from API is already encoded, compare directly with user.id
        return members.find(m => m.memberId === user.id);
    }, [members, user?.id]);

    const userAccessLevel = collection?.currentUserAccessLevel || currentUserMember?.accessLevel;
    
    // Check permissions: if user is in members list, they have access
    // Default to AUTHOR if user created the collection (role owner) or has AUTHOR access level
    // If user is in members but accessLevel is not set, assume they have at least CONTRIBUTOR access
    const isOwner = currentUserMember?.role === 'owner' || userAccessLevel === 'AUTHOR';
    const hasAccess = !!currentUserMember || userAccessLevel; // User has access if they're in members list
    
    // If user is in members list but no access level set, default to CONTRIBUTOR (can add papers)
    const effectiveAccessLevel = userAccessLevel || (currentUserMember ? 'CONTRIBUTOR' : null);
    
    const canAddPaper = effectiveAccessLevel === 'AUTHOR' || effectiveAccessLevel === 'CONTRIBUTOR' || isOwner;
    const canManageMembers = effectiveAccessLevel === 'AUTHOR' || isOwner;
    const canSetPriority = effectiveAccessLevel === 'AUTHOR' || isOwner;

    // Paper search query - fetch all papers
    const {data: paperSearchData, isLoading: isLoadingPaperSearch} = useQuery({
        queryKey: ['paper-search-all', paperSearchQuery],
        queryFn: async () => {
            // First, get the first page to know total pages
            const firstPage = await getPaginatedPapers(0, paperSearchApiPageSize, paperSearchQuery || undefined);
            const totalPages = firstPage?.data?.totalPages || 1;
            const allPapers = [...(firstPage?.data?.papers || [])];
            
            // Fetch remaining pages if there are more
            if (totalPages > 1) {
                const remainingPromises = [];
                for (let page = 1; page < totalPages; page++) {
                    remainingPromises.push(
                        getPaginatedPapers(page, paperSearchApiPageSize, paperSearchQuery || undefined)
                    );
                }
                const remainingResults = await Promise.all(remainingPromises);
                remainingResults.forEach(result => {
                    if (result?.data?.papers) {
                        allPapers.push(...result.data.papers);
                    }
                });
            }
            
            return {
                data: {
                    content: allPapers,
                    totalElements: allPapers.length,
                    totalPages: 1,
                }
            };
        },
        enabled: isAddPaperOpen,
    });

    // Filter out papers that are already in the collection
    const allAvailablePapers = useMemo(() => {
        if (!paperSearchData?.data?.content) return [];
        const existingPaperIds = papers?.map(p => p.paperId) || [];
        return paperSearchData.data.content.filter((paper: any) => !existingPaperIds.includes(paper.id));
    }, [paperSearchData, papers]);

    // Client-side pagination for dialog (10 papers per page)
    const paginatedAvailablePapers = useMemo(() => {
        if (!allAvailablePapers || allAvailablePapers.length === 0) return [];
        const startIndex = paperSearchDialogPage * paperSearchDialogPageSize;
        const endIndex = startIndex + paperSearchDialogPageSize;
        return allAvailablePapers.slice(startIndex, endIndex);
    }, [allAvailablePapers, paperSearchDialogPage, paperSearchDialogPageSize]);

    const totalDialogPages = Math.ceil((allAvailablePapers?.length || 0) / paperSearchDialogPageSize);

    // Client-side pagination for papers list (using papersWithProgress)
    const paginatedPapers = useMemo(() => {
        if (!papersWithProgress || papersWithProgress.length === 0) return [];
        const startIndex = papersPage * papersPageSize;
        const endIndex = startIndex + papersPageSize;
        return papersWithProgress.slice(startIndex, endIndex);
    }, [papersWithProgress, papersPage, papersPageSize]);

    const totalPapersPages = Math.ceil((papersWithProgress?.length || 0) / papersPageSize);

    // Reset pagination when papers list changes
    useEffect(() => {
        if (papersWithProgress && papersWithProgress.length > 0) {
            const maxPage = Math.max(0, totalPapersPages - 1);
            if (papersPage > maxPage) {
                setPapersPage(maxPage);
            }
        } else if (papersWithProgress && papersWithProgress.length === 0) {
            setPapersPage(0);
        }
    }, [papersWithProgress?.length, totalPapersPages, papersPage]);

    const addPaperMutation = useMutation({
        mutationFn: async ({ paperId, priority }: { paperId: string; priority: string }) => {
            if (!id || !user?.id || !paperId) throw new Error('Missing required fields');
            return await addPaperToCollection({
                collectionId: id,
                paperId: paperId,
                userId: user.id,
                priority: priority || 'MEDIUM',
            });
        },
        onSuccess: () => {
            toast.success('Paper added to collection successfully');
            queryClient.invalidateQueries({queryKey: ['collection-papers']});
            queryClient.invalidateQueries({queryKey: ['paper-search']});
            setIsAddPaperOpen(false);
        },
        onError: (error: Error) => {
            console.error('Add paper error:', error);
            const errorMessage = error.message || 'Failed to add paper to collection';
            if (errorMessage.includes('Unable to validate paper') || errorMessage.includes('Paper not found')) {
                toast.error('Paper not found in the system. Please try another paper.');
            } else {
                toast.error(errorMessage);
            }
        },
    });

    const handleAddPaper = (paperId: string) => {
        // Default priority is MEDIUM when adding paper
        addPaperMutation.mutate({ paperId, priority: 'MEDIUM' });
    };

    /**
     * Update paper priority only (status is now calculated automatically, read-only)
     */
    const updatePriorityMutation = useMutation({
        mutationFn: async () => {
            if (!id || !user?.id || !selectedPaper) throw new Error('Missing required fields');
            return await updatePaperStatus({
                collectionId: id,
                paperId: selectedPaper.paperId,
                userId: user.id,
                // Status is not sent - it's calculated automatically by backend
                priority: canSetPriority ? selectedPriority : undefined,
            });
        },
        onSuccess: () => {
            toast.success('Paper priority updated successfully');
            queryClient.invalidateQueries({queryKey: ['collection-papers']});
            setIsPriorityDialogOpen(false);
            setSelectedPaper(null);
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to update paper priority');
        },
    });

    const removePaperMutation = useMutation({
        mutationFn: async (paper: CollectionPaperDetailResponse) => {
            if (!id || !user?.id) throw new Error('Missing required fields');
            return await removePaperFromCollection(id, paper.paperId, user.id);
        },
        onSuccess: () => {
            toast.success('Paper removed from collection successfully');
            queryClient.invalidateQueries({queryKey: ['collection-papers']});
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to remove paper from collection');
        },
    });

    const addMemberMutation = useMutation({
        mutationFn: async () => {
            if (!id || !newMemberEmail) throw new Error('Missing required fields');
            
            // Add member directly by email
            return await addMemberToCollectionByEmail(id, newMemberEmail, newMemberAccessLevel);
        },
        onSuccess: () => {
            toast.success('Member added to collection successfully');
            queryClient.invalidateQueries({queryKey: ['collection-members']});
            setIsAddMemberOpen(false);
            setNewMemberEmail('');
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to add member to collection');
        },
    });

    const removeMemberMutation = useMutation({
        mutationFn: async (member: CollectionUserResponse) => {
            if (!id) throw new Error('Collection ID is required');
            return await removeMemberFromCollection(id, member.memberId);
        },
        onSuccess: () => {
            toast.success('Member removed from collection successfully');
            queryClient.invalidateQueries({queryKey: ['collection-members']});
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to remove member from collection');
        },
    });

    /**
     * Handle priority click (status is now read-only, calculated automatically)
     * Only AUTHOR can change priority
     */
    const handlePriorityClick = (paper: CollectionPaperDetailResponse) => {
        if (!canSetPriority) {
            toast.error('Only collection authors can change paper priority');
            return;
        }
        setSelectedPaper(paper);
        setSelectedPriority(paper.priority || 'MEDIUM');
        setIsPriorityDialogOpen(true);
    };

    const handleRemovePaper = (paper: CollectionPaperDetailResponse) => {
        if (window.confirm(`Are you sure you want to remove "${paper.title}" from this collection?`)) {
            removePaperMutation.mutate(paper);
        }
    };

    const handleRemoveMember = (member: CollectionUserResponse) => {
        if (window.confirm(`Are you sure you want to remove "${member.memberName}" from this collection?`)) {
            removeMemberMutation.mutate(member);
        }
    };

    const getStatusColor = (status?: string) => {
        switch (status) {
            case 'Finished':
                return 'bg-green-500';
            case 'Reading':
                return 'bg-blue-500';
            case 'ToRead':
                return 'bg-gray-500';
            default:
                return 'bg-gray-500';
        }
    };

    const getPriorityColor = (priority?: string) => {
        switch (priority) {
            case 'HIGH':
                return 'bg-red-500';
            case 'MEDIUM':
                return 'bg-yellow-500';
            case 'LOW':
                return 'bg-green-500';
            default:
                return 'bg-gray-500';
        }
    };

    if (isLoadingCollection) {
        return (
            <div className="min-h-screen bg-background">
                <Header/>
                <div className="flex justify-center items-center min-h-[60vh]">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                </div>
            </div>
        );
    }

    if (!collection) {
        return (
            <div className="min-h-screen bg-background">
                <Header/>
                <div className="container mx-auto px-4 py-8">
                    <Card>
                        <CardContent className="text-center py-12">
                            <p className="text-muted-foreground">Collection not found</p>
                            <Button onClick={() => navigate('/collections')} className="mt-4">
                                Back to Collections
                            </Button>
                        </CardContent>
                    </Card>
                </div>
            </div>
        );
    }

    return (
        <>
            <Helmet>
                <title>LabVerse | {collection.name}</title>
            </Helmet>
            <div className="min-h-screen bg-background">
                <Header/>

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="space-y-6">
                        <CollectionHeader 
                            collection={collection} 
                            onBack={() => navigate('/collections')} 
                        />

                        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'papers' | 'members' | 'progress')}>
                            <div className="flex items-center justify-between">
                                <TabsList>
                                    <TabsTrigger value="papers">
                                        <FileText className="h-4 w-4 mr-2"/>
                                        Papers
                                    </TabsTrigger>
                                    <TabsTrigger value="members">
                                        <Users className="h-4 w-4 mr-2"/>
                                        Members
                                    </TabsTrigger>
                                    {isOwner && (
                                        <TabsTrigger value="progress">
                                            <BarChart3 className="h-4 w-4 mr-2"/>
                                            Progress
                                        </TabsTrigger>
                                    )}
                                </TabsList>
                                {activeTab === 'papers' && canAddPaper && (
                                    <AddPaperDialog
                                        open={isAddPaperOpen}
                                        onOpenChange={setIsAddPaperOpen}
                                        paperSearchQuery={paperSearchQuery}
                                        setPaperSearchQuery={setPaperSearchQuery}
                                        isLoadingPaperSearch={isLoadingPaperSearch}
                                        paginatedAvailablePapers={paginatedAvailablePapers}
                                        paperSearchDialogPage={paperSearchDialogPage}
                                        setPaperSearchDialogPage={setPaperSearchDialogPage}
                                        totalDialogPages={totalDialogPages}
                                        addPaperMutation={addPaperMutation}
                                        handleAddPaper={handleAddPaper}
                                    />
                                )}
                                {activeTab === 'members' && canManageMembers && (
                                    <AddMemberDialog
                                        open={isAddMemberOpen}
                                        onOpenChange={setIsAddMemberOpen}
                                        newMemberEmail={newMemberEmail}
                                        setNewMemberEmail={setNewMemberEmail}
                                        newMemberAccessLevel={newMemberAccessLevel}
                                        setNewMemberAccessLevel={setNewMemberAccessLevel}
                                        addMemberMutation={addMemberMutation}
                                    />
                                )}
                            </div>

                            <TabsContent value="papers" className="mt-6">
                                <PapersTab
                                    isLoadingPapers={isLoadingPapers}
                                    papers={papersWithProgress}
                                    paginatedPapers={paginatedPapers}
                                    papersPage={papersPage}
                                    setPapersPage={setPapersPage}
                                    totalPapersPages={totalPapersPages}
                                    canAddPaper={canAddPaper}
                                    canSetPriority={canSetPriority}
                                    handleRemovePaper={handleRemovePaper}
                                    handlePriorityClick={handlePriorityClick}
                                    getStatusColor={getStatusColor}
                                    getPriorityColor={getPriorityColor}
                                />
                                {canAddPaper && papersWithProgress && papersWithProgress.length === 0 && (
                                    <div className="text-center mt-4">
                                                <Button onClick={() => setIsAddPaperOpen(true)}>
                                                    <Plus className="h-4 w-4 mr-2"/>
                                                    Add Paper
                                                </Button>
                                    </div>
                                )}
                            </TabsContent>

                            <TabsContent value="members" className="mt-6">
                                <MembersTab
                                    isLoadingMembers={isLoadingMembers}
                                    members={members}
                                    canManageMembers={canManageMembers}
                                    currentUserId={user?.id}
                                    handleRemoveMember={handleRemoveMember}
                                />
                            </TabsContent>

                            {isOwner && (
                                <TabsContent value="progress" className="mt-6">
                                    {id && (
                                        <ProgressDashboard 
                                            collectionId={id} 
                                            members={members}
                                        />
                                    )}
                                </TabsContent>
                            )}
                        </Tabs>

                        <PriorityDialog
                            open={isPriorityDialogOpen}
                            onOpenChange={setIsPriorityDialogOpen}
                            selectedPaper={selectedPaper}
                            selectedPriority={selectedPriority}
                            setSelectedPriority={setSelectedPriority}
                            updatePriorityMutation={updatePriorityMutation}
                        />
                    </div>
                </main>
            </div>
        </>
    );
};

export default CollectionDetails;

