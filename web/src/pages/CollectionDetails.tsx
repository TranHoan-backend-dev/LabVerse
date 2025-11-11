import {useParams, useNavigate} from "react-router-dom";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {useState, useMemo, useEffect} from "react";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {ArrowLeft, Plus, Trash2, Users, FileText, Edit, Search, Loader2} from "lucide-react";
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
    type CollectionResponse,
    type CollectionPaperDetailResponse,
    type CollectionUserResponse
} from "@/services/collection.service";
import {BASE_API_URL} from "@/type/constant.ts";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {getPaginatedPapers} from "@/services/paper.service.ts";
import {getAuthHeaders} from "@/utils/token";

const CollectionDetails = () => {
    const {id} = useParams<{id: string}>();
    const {user} = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [isAddPaperOpen, setIsAddPaperOpen] = useState(false);
    const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
    const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
    const [selectedPaper, setSelectedPaper] = useState<CollectionPaperDetailResponse | null>(null);
    const [selectedStatus, setSelectedStatus] = useState<string>('ToRead');
    const [selectedPriority, setSelectedPriority] = useState<string>('MEDIUM');
    const [newPaperId, setNewPaperId] = useState('');
    const [newMemberEmail, setNewMemberEmail] = useState('');
    const [newMemberAccessLevel, setNewMemberAccessLevel] = useState<'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR'>('CONTRIBUTOR');
    const [activeTab, setActiveTab] = useState<'papers' | 'members'>('papers');
    
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
            const allPapers = [...(firstPage?.data?.content || [])];
            
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

    // Client-side pagination for papers list
    const paginatedPapers = useMemo(() => {
        if (!papers || papers.length === 0) return [];
        const startIndex = papersPage * papersPageSize;
        const endIndex = startIndex + papersPageSize;
        return papers.slice(startIndex, endIndex);
    }, [papers, papersPage, papersPageSize]);

    const totalPapersPages = Math.ceil((papers?.length || 0) / papersPageSize);

    // Reset pagination when papers list changes
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
            queryClient.invalidateQueries({queryKey: ['collection-papers']});
            queryClient.invalidateQueries({queryKey: ['paper-search']});
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

    const updateStatusMutation = useMutation({
        mutationFn: async () => {
            if (!id || !user?.id || !selectedPaper) throw new Error('Missing required fields');
            return await updatePaperStatus({
                collectionId: id,
                paperId: selectedPaper.paperId,
                userId: user.id,
                status: selectedStatus,
                priority: canSetPriority ? selectedPriority : undefined,
            });
        },
        onSuccess: () => {
            toast.success('Paper status updated successfully');
            queryClient.invalidateQueries({queryKey: ['collection-papers']});
            setIsStatusDialogOpen(false);
            setSelectedPaper(null);
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to update paper status');
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

    const handleStatusClick = (paper: CollectionPaperDetailResponse) => {
        setSelectedPaper(paper);
        setSelectedStatus(paper.status || 'ToRead');
        setSelectedPriority(paper.priority || 'MEDIUM');
        setIsStatusDialogOpen(true);
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
                                <ArrowLeft className="h-4 w-4 mr-2"/>
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
                        <div className="flex items-center gap-4">
                            <Button variant="ghost" size="icon" onClick={() => navigate('/collections')}>
                                <ArrowLeft className="h-5 w-5"/>
                            </Button>
                            <div className="flex-1">
                                <h1 className="text-3xl font-bold">{collection.name}</h1>
                                <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                                    <span>Papers: {collection.paperCount || 0}</span>
                                    <span>Members: {collection.memberCount || 0}</span>
                                    {collection.currentUserAccessLevel && (
                                        <Badge variant="outline">{collection.currentUserAccessLevel}</Badge>
                                    )}
                                </div>
                            </div>
                        </div>

                        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'papers' | 'members')}>
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
                                </TabsList>
                                {activeTab === 'papers' && canAddPaper && (
                                    <Dialog open={isAddPaperOpen}                                     onOpenChange={(open) => {
                                        setIsAddPaperOpen(open);
                                        if (!open) {
                                            setPaperSearchQuery('');
                                            setPaperSearchDialogPage(0);
                                        }
                                    }}>
                                        <DialogTrigger asChild>
                                            <Button>
                                                <Plus className="h-4 w-4 mr-2"/>
                                                Add Paper
                                            </Button>
                                        </DialogTrigger>
                                        <DialogContent className="max-w-3xl max-h-[80vh] overflow-hidden flex flex-col">
                                            <DialogHeader>
                                                <DialogTitle>Add Paper to Collection</DialogTitle>
                                            </DialogHeader>
                                            <div className="flex flex-col gap-4 flex-1 overflow-hidden">
                                                <div className="relative">
                                                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground"/>
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
                                                            <Loader2 className="h-8 w-8 animate-spin text-muted-foreground"/>
                                                        </div>
                                                    ) : allAvailablePapers && allAvailablePapers.length > 0 ? (
                                                        <>
                                                            {paginatedAvailablePapers.map((paper: any) => (
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
                                                                    <Loader2 className="h-4 w-4 mr-2 animate-spin"/>
                                                                    Adding...
                                                                </>
                                                            ) : (
                                                                <>
                                                                    <Plus className="h-4 w-4 mr-2"/>
                                                                    Add to Collection
                                                                </>
                                                            )}
                                                                        </Button>
                                                                    </CardContent>
                                                                </Card>
                                                            ))}
                                                            {allAvailablePapers && allAvailablePapers.length > 0 && (
                                                                <div className="flex items-center justify-center gap-3 pt-4 border-t">
                                                                    <Button
                                                                        variant="outline"
                                                                        size="sm"
                                                                        onClick={() => setPaperSearchDialogPage(p => Math.max(0, p - 1))}
                                                                        disabled={paperSearchDialogPage === 0 || isLoadingPaperSearch || totalDialogPages <= 1}
                                                                    >
                                                                        Previous
                                                                    </Button>
                                                                    <span className="text-sm text-muted-foreground min-w-[140px] text-center">
                                                                        Page {paperSearchDialogPage + 1} of {totalDialogPages}
                                                                        {allAvailablePapers.length > 0 && (
                                                                            <span className="block text-xs mt-1">
                                                                                ({allAvailablePapers.length} total papers)
                                                                            </span>
                                                                        )}
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
                                                            )}
                                                        </>
                                                    ) : (
                                                        <div className="text-center py-12">
                                                            <FileText className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
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
                                )}
                                {activeTab === 'members' && canManageMembers && (
                                    <Dialog open={isAddMemberOpen} onOpenChange={(open) => {
                                        setIsAddMemberOpen(open);
                                        if (!open) {
                                            setNewMemberEmail('');
                                            setNewMemberAccessLevel('CONTRIBUTOR');
                                        }
                                    }}>
                                        <DialogTrigger asChild>
                                            <Button>
                                                <Plus className="h-4 w-4 mr-2"/>
                                                Invite Member
                                            </Button>
                                        </DialogTrigger>
                                        <DialogContent>
                                            <DialogHeader>
                                                <DialogTitle>Invite Member to Collection</DialogTitle>
                                            </DialogHeader>
                                            <div className="space-y-4">
                                                <div className="space-y-2">
                                                    <Label htmlFor="member-email">Member Email</Label>
                                                    <Input
                                                        id="member-email"
                                                        type="email"
                                                        value={newMemberEmail}
                                                        onChange={(e) => setNewMemberEmail(e.target.value)}
                                                        placeholder="Enter member email address"
                                                        onKeyDown={(e) => {
                                                            if (e.key === 'Enter' && newMemberEmail && !addMemberMutation.isPending) {
                                                                addMemberMutation.mutate();
                                                            }
                                                        }}
                                                    />
                                                    <p className="text-xs text-muted-foreground">
                                                        Enter the email address of the user you want to invite. They must have an account on LabVerse.
                                                    </p>
                                                </div>
                                                <div className="space-y-2">
                                                    <Label htmlFor="access-level">Access Level</Label>
                                                    <Select
                                                        value={newMemberAccessLevel}
                                                        onValueChange={(v) => setNewMemberAccessLevel(v as typeof newMemberAccessLevel)}
                                                    >
                                                        <SelectTrigger>
                                                            <SelectValue/>
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            <SelectItem value="READ_ONLY">Read Only - Can view papers only</SelectItem>
                                                            <SelectItem value="CONTRIBUTOR">Contributor - Can add papers and update status</SelectItem>
                                                            <SelectItem value="AUTHOR">Author - Full access including priority and members</SelectItem>
                                                        </SelectContent>
                                                    </Select>
                                                </div>
                                                <div className="flex gap-2">
                                                    <Button
                                                        variant="outline"
                                                        onClick={() => setIsAddMemberOpen(false)}
                                                        className="flex-1"
                                                    >
                                                        Cancel
                                                    </Button>
                                                    <Button
                                                        onClick={() => addMemberMutation.mutate()}
                                                        disabled={!newMemberEmail || addMemberMutation.isPending || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newMemberEmail)}
                                                        className="flex-1"
                                                    >
                                                        {addMemberMutation.isPending ? (
                                                            <>
                                                                <Loader2 className="h-4 w-4 mr-2 animate-spin"/>
                                                                Inviting...
                                                            </>
                                                        ) : (
                                                            <>
                                                                <Users className="h-4 w-4 mr-2"/>
                                                                Send Invite
                                                            </>
                                                        )}
                                                    </Button>
                                                </div>
                                            </div>
                                        </DialogContent>
                                    </Dialog>
                                )}
                            </div>

                            <TabsContent value="papers" className="mt-6">
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
                                                                    <Badge
                                                                        className={`${getStatusColor(paper.status)} text-white cursor-pointer`}
                                                                        onClick={() => handleStatusClick(paper)}
                                                                    >
                                                                        {paper.status}
                                                                    </Badge>
                                                                )}
                                                                {paper.priority && (
                                                                    <Badge
                                                                        variant="outline"
                                                                        className={`${getPriorityColor(paper.priority)} text-white cursor-pointer`}
                                                                        onClick={() => canSetPriority && handleStatusClick(paper)}
                                                                    >
                                                                        {paper.priority}
                                                                    </Badge>
                                                                )}
                                                                <Button
                                                                    variant="ghost"
                                                                    size="icon"
                                                                    onClick={() => handleRemovePaper(paper)}
                                                                >
                                                                    <Trash2 className="h-4 w-4"/>
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
                                                    onClick={() => setPapersPage(p => Math.max(0, p - 1))}
                                                    disabled={papersPage === 0 || totalPapersPages <= 1}
                                                >
                                                    Previous
                                                </Button>
                                                <span className="text-sm text-muted-foreground min-w-[120px] text-center">
                                                    Page {papersPage + 1} of {totalPapersPages}
                                                    <span className="block text-xs mt-1">
                                                        ({papers.length} total papers)
                                                    </span>
                                                </span>
                                                <Button
                                                    variant="outline"
                                                    size="sm"
                                                    onClick={() => setPapersPage(p => Math.min(totalPapersPages - 1, p + 1))}
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
                                            <FileText className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                                            <h3 className="text-lg font-semibold mb-2">No papers yet</h3>
                                            <p className="text-muted-foreground mb-4">
                                                {canAddPaper
                                                    ? 'Add papers to this collection to get started'
                                                    : 'No papers have been added to this collection'}
                                            </p>
                                            {canAddPaper && (
                                                <Button onClick={() => setIsAddPaperOpen(true)}>
                                                    <Plus className="h-4 w-4 mr-2"/>
                                                    Add Paper
                                                </Button>
                                            )}
                                        </CardContent>
                                    </Card>
                                )}
                            </TabsContent>

                            <TabsContent value="members" className="mt-6">
                                {isLoadingMembers ? (
                                    <div className="flex justify-center py-12">
                                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                                    </div>
                                ) : members && members.length > 0 ? (
                                    <div className="grid gap-4">
                                        {members.map((member) => (
                                            <Card key={member.memberId} className="hover:shadow-md transition-shadow">
                                                <CardHeader>
                                                    <div className="flex items-start justify-between">
                                                        <div className="flex-1">
                                                            <CardTitle className="text-lg font-semibold mb-1">
                                                                {member.memberName}
                                                            </CardTitle>
                                                            <p className="text-xs text-muted-foreground mb-3">
                                                                {member.memberEmail}
                                                            </p>
                                                            <div className="flex gap-2">
                                                                <Badge variant="outline">{member.accessLevel}</Badge>
                                                                <Badge variant="secondary">{member.role}</Badge>
                                                            </div>
                                                        </div>
                                                        {canManageMembers && member.memberId !== user?.id && (
                                                            <Button
                                                                variant="ghost"
                                                                size="icon"
                                                                onClick={() => handleRemoveMember(member)}
                                                                className="flex-shrink-0"
                                                            >
                                                                <Trash2 className="h-4 w-4"/>
                                                            </Button>
                                                        )}
                                                    </div>
                                                </CardHeader>
                                            </Card>
                                        ))}
                                    </div>
                                ) : (
                                    <Card className="text-center py-12">
                                        <CardContent>
                                            <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                                            <h3 className="text-lg font-semibold mb-2">No members yet</h3>
                                            <p className="text-muted-foreground">
                                                {canManageMembers
                                                    ? 'Add members to this collection to start collaborating'
                                                    : 'No members have been added to this collection'}
                                            </p>
                                        </CardContent>
                                    </Card>
                                )}
                            </TabsContent>
                        </Tabs>

                        {/* Status/Priority Update Dialog */}
                        <Dialog open={isStatusDialogOpen} onOpenChange={setIsStatusDialogOpen}>
                            <DialogContent>
                                <DialogHeader>
                                    <DialogTitle>Update Paper Status</DialogTitle>
                                </DialogHeader>
                                {selectedPaper && (
                                    <div className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="status">Status</Label>
                                            <Select
                                                value={selectedStatus}
                                                onValueChange={setSelectedStatus}
                                            >
                                                <SelectTrigger>
                                                    <SelectValue/>
                                                </SelectTrigger>
                                                <SelectContent>
                                                    <SelectItem value="ToRead">To Read</SelectItem>
                                                    <SelectItem value="Reading">Reading</SelectItem>
                                                    <SelectItem value="Finished">Finished</SelectItem>
                                                </SelectContent>
                                            </Select>
                                        </div>
                                        {canSetPriority && (
                                            <div className="space-y-2">
                                                <Label htmlFor="priority">Priority</Label>
                                                <Select
                                                    value={selectedPriority}
                                                    onValueChange={setSelectedPriority}
                                                >
                                                    <SelectTrigger>
                                                        <SelectValue/>
                                                    </SelectTrigger>
                                                    <SelectContent>
                                                        <SelectItem value="HIGH">High</SelectItem>
                                                        <SelectItem value="MEDIUM">Medium</SelectItem>
                                                        <SelectItem value="LOW">Low</SelectItem>
                                                    </SelectContent>
                                                </Select>
                                            </div>
                                        )}
                                        <Button
                                            onClick={() => updateStatusMutation.mutate()}
                                            disabled={updateStatusMutation.isPending}
                                            className="w-full"
                                        >
                                            {updateStatusMutation.isPending ? 'Updating...' : 'Update Status'}
                                        </Button>
                                    </div>
                                )}
                            </DialogContent>
                        </Dialog>
                    </div>
                </main>
            </div>
        </>
    );
};

export default CollectionDetails;

