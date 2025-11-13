import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useState, useMemo, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { ArrowLeft } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import { Helmet } from "react-helmet-async";
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
import { getPaginatedPapers } from "@/services/paper.service.ts";
import { Tabs, TabsContent } from "@/components/ui/tabs";
import CollectionHeader from "./components/CollectionHeader";
import PapersTab from "./components/PapersTab";
import MembersTab from "./components/MembersTab";
import ProgressDashboard from "./components/ProgressDashboard";
import PriorityDialog from "./components/PriorityDialog";

const CollectionDetails = () => {
    const { id } = useParams<{ id: string }>();
    const { user } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [isAddPaperOpen, setIsAddPaperOpen] = useState(false);
    const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
    const [isPriorityDialogOpen, setIsPriorityDialogOpen] = useState(false);
    const [selectedPaper, setSelectedPaper] = useState<CollectionPaperDetailResponse | null>(null);
    const [selectedPriority, setSelectedPriority] = useState<string>("MEDIUM");
    const [newMemberEmail, setNewMemberEmail] = useState('');
    const [newMemberAccessLevel, setNewMemberAccessLevel] = useState<'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR'>('CONTRIBUTOR');
    const [activeTab, setActiveTab] = useState<'papers' | 'members' | 'progress'>('papers');

    // Papers list pagination
    const [papersPage, setPapersPage] = useState(0);
    const papersPageSize = 10;

    // Paper search states (used in AddPaper dialog)
    const [paperSearchQuery, setPaperSearchQuery] = useState('');
    const [paperSearchDialogPage, setPaperSearchDialogPage] = useState(0);
    const paperSearchDialogPageSize = 10;
    const paperSearchApiPageSize = 100;

    const { data: collection, isLoading: isLoadingCollection } = useQuery({
        queryKey: ['collection', id],
        queryFn: async () => {
            if (!id) throw new Error('Collection ID is required');
            return await getCollectionById(id);
        },
        enabled: !!id && !!user,
    });

    const { data: papers, isLoading: isLoadingPapers } = useQuery({
        queryKey: ['collection-papers', id],
        queryFn: async () => {
            if (!id) throw new Error('Collection ID is required');
            return await getPapersInCollection(id);
        },
        enabled: !!id && !!user && activeTab === 'papers',
    });

    const { data: members, isLoading: isLoadingMembers } = useQuery({
        queryKey: ['collection-members', id],
        queryFn: async () => {
            if (!id) throw new Error('Collection ID is required');
            return await getCollectionMembers(id);
        },
        enabled: !!id && !!user,
    });

    const currentUserMember = useMemo(() => {
        if (!members || !user?.id) return null;
        return members.find(m => m.memberId === user.id);
    }, [members, user?.id]);

    const userAccessLevel = collection?.currentUserAccessLevel || currentUserMember?.accessLevel;
    const isOwner = currentUserMember?.role === 'owner' || userAccessLevel === 'AUTHOR';
    const effectiveAccessLevel = userAccessLevel || (currentUserMember ? 'CONTRIBUTOR' : null);
    const canAddPaper = effectiveAccessLevel === 'AUTHOR' || effectiveAccessLevel === 'CONTRIBUTOR' || isOwner;
    const canManageMembers = effectiveAccessLevel === 'AUTHOR' || isOwner;
    const canSetPriority = effectiveAccessLevel === 'AUTHOR' || isOwner;

    // Paper search query - fetch all papers (used by AddPaper dialog)
    const { data: paperSearchData, isLoading: isLoadingPaperSearch } = useQuery({
        queryKey: ['paper-search-all', paperSearchQuery],
        queryFn: async () => {
            const firstPage = await getPaginatedPapers(0, paperSearchApiPageSize, paperSearchQuery || undefined);
            const totalPages = firstPage?.data?.totalPages || 1;
            const allPapers = [...(firstPage?.data?.content || [])];
            if (totalPages > 1) {
                const remainingPromises = [];
                for (let page = 1; page < totalPages; page++) {
                    remainingPromises.push(
                        getPaginatedPapers(page, paperSearchApiPageSize, paperSearchQuery || undefined)
                    );
                }
                const remainingResults = await Promise.all(remainingPromises);
                remainingResults.forEach(result => {
                    if (result?.data?.content) {
                        allPapers.push(...result.data.content);
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

    const allAvailablePapers = useMemo(() => {
        if (!paperSearchData?.data?.content) return [];
        const existingPaperIds = papers?.map((p) => p.paperId) || [];
        return paperSearchData.data.content.filter((paper) => !existingPaperIds.includes(paper.id));
    }, [paperSearchData, papers]);

    const paginatedAvailablePapers = useMemo(() => {
        if (!allAvailablePapers || allAvailablePapers.length === 0) return [];
        const startIndex = paperSearchDialogPage * paperSearchDialogPageSize;
        const endIndex = startIndex + paperSearchDialogPageSize;
        return allAvailablePapers.slice(startIndex, endIndex);
    }, [allAvailablePapers, paperSearchDialogPage, paperSearchDialogPageSize]);

    const totalDialogPages = Math.ceil((allAvailablePapers?.length || 0) / paperSearchDialogPageSize);

    const paginatedPapers = useMemo(() => {
        if (!papers || papers.length === 0) return [];
        const startIndex = papersPage * papersPageSize;
        const endIndex = startIndex + papersPageSize;
        return papers.slice(startIndex, endIndex);
    }, [papers, papersPage, papersPageSize]);

    const totalPapersPages = Math.ceil((papers?.length || 0) / papersPageSize);

    useEffect(() => {
        if (papers && papers.length > 0) {
            const maxPage = Math.max(0, totalPapersPages - 1);
            if (papersPage > maxPage) {
                setPapersPage(maxPage);
            }
        } else if (papers && papers.length === 0) {
            setPapersPage(0);
        }
    }, [papers?.length, totalPapersPages, papersPage]);

    const addPaperMutation = useMutation({
        mutationFn: async (paperId: string) => {
            if (!id || !user?.id || !paperId) throw new Error('Missing required fields');
            return await addPaperToCollection({
                collectionId: id,
                paperId: paperId,
                userId: user.id,
            });
        },
        onSuccess: () => {
            toast.success('Paper added to collection successfully');
            queryClient.invalidateQueries({ queryKey: ['collection-papers'] });
            queryClient.invalidateQueries({ queryKey: ['paper-search'] });
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
        addPaperMutation.mutate(paperId);
    };

    const updatePriorityMutation = useMutation({
        mutationFn: async () => {
            if (!id || !user?.id || !selectedPaper) throw new Error('Missing required fields');
            return await updatePaperStatus({
                collectionId: id,
                paperId: selectedPaper.paperId,
                userId: user.id,
                priority: canSetPriority ? selectedPriority : undefined,
            });
        },
        onSuccess: () => {
            toast.success('Paper priority updated successfully');
            queryClient.invalidateQueries({ queryKey: ['collection-papers'] });
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
            queryClient.invalidateQueries({ queryKey: ['collection-papers'] });
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to remove paper from collection');
        },
    });

    const addMemberMutation = useMutation({
        mutationFn: async () => {
            if (!id || !newMemberEmail) throw new Error('Missing required fields');
            return await addMemberToCollectionByEmail(id, newMemberEmail, newMemberAccessLevel);
        },
        onSuccess: () => {
            toast.success('Member added to collection successfully');
            queryClient.invalidateQueries({ queryKey: ['collection-members'] });
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
            queryClient.invalidateQueries({ queryKey: ['collection-members'] });
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to remove member from collection');
        },
    });

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
                <Header />
                <div className="flex justify-center items-center min-h-[60vh]">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                </div>
            </div>
        );
    }

    if (!collection) {
        return (
            <div className="min-h-screen bg-background">
                <Header />
                <div className="container mx-auto px-4 py-8">
                    <Card>
                        <CardContent className="text-center py-12">
                            <p className="text-muted-foreground">Collection not found</p>
                            <Button onClick={() => navigate('/collections')} className="mt-4">
                                <ArrowLeft className="h-4 w-4 mr-2" />
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
                <Header />

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="space-y-6">
                        <CollectionHeader
                            collection={collection}
                            navigate={navigate}
                            activeTab={activeTab}
                            setActiveTab={setActiveTab}
                            isOwner={isOwner}
                            canAddPaper={canAddPaper}
                            canManageMembers={canManageMembers}
                            openAddPaper={() => setIsAddPaperOpen(true)}
                            openAddMember={() => setIsAddMemberOpen(true)}
                        />

                        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'papers' | 'members' | 'progress')}>
                            <TabsContent value="papers" className="mt-6">
                                <PapersTab
                                    isLoadingPapers={isLoadingPapers}
                                    papers={papers}
                                    paginatedPapers={paginatedPapers}
                                    papersPage={papersPage}
                                    setPapersPage={setPapersPage}
                                    totalPapersPages={totalPapersPages}
                                    canAddPaper={canAddPaper}
                                    isAddPaperOpen={isAddPaperOpen}
                                    setIsAddPaperOpen={setIsAddPaperOpen}
                                    paperSearchQuery={paperSearchQuery}
                                    setPaperSearchQuery={setPaperSearchQuery}
                                    isLoadingPaperSearch={isLoadingPaperSearch}
                                    paginatedAvailablePapers={paginatedAvailablePapers}
                                    paperSearchDialogPage={paperSearchDialogPage}
                                    setPaperSearchDialogPage={setPaperSearchDialogPage}
                                    totalDialogPages={totalDialogPages}
                                    addPaperMutation={addPaperMutation}
                                    handleAddPaper={handleAddPaper}
                                    handleRemovePaper={handleRemovePaper}
                                    handlePriorityClick={handlePriorityClick}
                                    getStatusColor={getStatusColor}
                                    getPriorityColor={getPriorityColor}
                                />
                            </TabsContent>

                            <TabsContent value="members" className="mt-6">
                                <MembersTab
                                    isLoadingMembers={isLoadingMembers}
                                    members={members}
                                    canManageMembers={canManageMembers}
                                    isAddMemberOpen={isAddMemberOpen}
                                    setIsAddMemberOpen={setIsAddMemberOpen}
                                    newMemberEmail={newMemberEmail}
                                    setNewMemberEmail={setNewMemberEmail}
                                    newMemberAccessLevel={newMemberAccessLevel}
                                    setNewMemberAccessLevel={setNewMemberAccessLevel}
                                    addMemberMutation={addMemberMutation}
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
