import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {useNavigate} from "react-router-dom";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Plus, Users, Edit, Trash2, MoreVertical} from "lucide-react";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import Header from "@/components/Header";
import {
    createCollection,
    getMyCollections,
    getSharedCollections,
    updateCollection,
    deleteCollection,
    type CollectionResponse
} from "@/services/collection.service";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

const Collections = () => {
    const {user} = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [editingCollection, setEditingCollection] = useState<CollectionResponse | null>(null);
    const [newCollection, setNewCollection] = useState({name: ''});
    const [editName, setEditName] = useState('');
    const [activeTab, setActiveTab] = useState<'my' | 'shared'>('my');

    const {data: myCollectionsData, isLoading: isLoadingMy} = useQuery({
        queryKey: ['collections', 'my', user?.id],
        queryFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await getMyCollections(user.id);
        },
        enabled: !!user && activeTab === 'my',
    });

    const {data: sharedCollectionsData, isLoading: isLoadingShared} = useQuery({
        queryKey: ['collections', 'shared', user?.id],
        queryFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await getSharedCollections(user.id);
        },
        enabled: !!user && activeTab === 'shared',
    });

    const collections = activeTab === 'my' 
        ? myCollectionsData?.content || []
        : sharedCollectionsData?.content || [];
    const isLoading = activeTab === 'my' ? isLoadingMy : isLoadingShared;

    const createMutation = useMutation({
        mutationFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await createCollection(newCollection.name, user.id);
        },
        onSuccess: () => {
            toast.success('Collection created successfully');
            queryClient.invalidateQueries({queryKey: ['collections']});
            setIsCreateOpen(false);
            setNewCollection({name: ''});
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to create collection');
        },
    });

    const updateMutation = useMutation({
        mutationFn: async () => {
            if (!user?.id || !editingCollection) throw new Error('Invalid request');
            return await updateCollection(editingCollection.id, editName, user.id);
        },
        onSuccess: () => {
            toast.success('Collection updated successfully');
            queryClient.invalidateQueries({queryKey: ['collections']});
            setIsEditOpen(false);
            setEditingCollection(null);
            setEditName('');
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to update collection');
        },
    });

    const deleteMutation = useMutation({
        mutationFn: async (collection: CollectionResponse) => {
            if (!user?.id) throw new Error('User not logged in');
            return await deleteCollection(collection.id, user.id);
        },
        onSuccess: () => {
            toast.success('Collection deleted successfully');
            queryClient.invalidateQueries({queryKey: ['collections']});
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to delete collection');
        },
    });

    const handleEdit = (collection: CollectionResponse) => {
        setEditingCollection(collection);
        setEditName(collection.name);
        setIsEditOpen(true);
    };

    const handleDelete = (collection: CollectionResponse) => {
        if (window.confirm(`Are you sure you want to delete "${collection.name}"? This action cannot be undone.`)) {
            deleteMutation.mutate(collection);
        }
    };

    const handleCollectionClick = (collection: CollectionResponse) => {
        navigate(`/collections/${collection.id}`);
    };

    return (
        <>
            <Helmet>
                <title>LabVerse | Research Group Collections</title>
                <meta
                    name="description"
                    content="Manage and share your research collections with your team on LabVerse. Create, collaborate, and organize papers intelligently."
                />
                <meta property="og:title" content="LabVerse - Research Group Collections"/>
                <meta
                    property="og:description"
                    content="Create and manage research collections, and collaborate seamlessly with colleagues on LabVerse."
                />
                <meta property="og:image" content="/og-collections.png"/>
                <meta property="og:type" content="website"/>
                <meta property="og:url" content="https://labverse.app/collections"/>
                <link rel="canonical" href="https://labverse.app/collections"/>
            </Helmet>
            <div className="min-h-screen bg-background">
                <Header/>

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="space-y-8">
                        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                            <div>
                                <h1 className="text-3xl font-bold mb-2">Team Collections</h1>
                                <p className="text-muted-foreground">
                                    Collaborate on research with your team
                                </p>
                            </div>

                            <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                                <DialogTrigger asChild>
                                    <Button size="lg" className="sm:w-auto w-full">
                                        <Plus className="h-5 w-5 mr-2"/>
                                        Create Collection
                                    </Button>
                                </DialogTrigger>
                                <DialogContent>
                                    <DialogHeader>
                                        <DialogTitle>Create New Collection</DialogTitle>
                                    </DialogHeader>
                                    <div className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="name">Collection Name</Label>
                                            <Input
                                                id="name"
                                                value={newCollection.name}
                                                onChange={(e) => setNewCollection({
                                                    ...newCollection,
                                                    name: e.target.value
                                                })}
                                                placeholder="e.g., Machine Learning Papers"
                                            />
                                        </div>
                                        <Button
                                            onClick={() => createMutation.mutate()}
                                            disabled={!newCollection.name || createMutation.isPending}
                                            className="w-full"
                                        >
                                            {createMutation.isPending ? 'Creating...' : 'Create Collection'}
                                        </Button>
                                    </div>
                                </DialogContent>
                            </Dialog>

                            <Dialog open={isEditOpen} onOpenChange={setIsEditOpen}>
                                <DialogContent>
                                    <DialogHeader>
                                        <DialogTitle>Edit Collection</DialogTitle>
                                    </DialogHeader>
                                    <div className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="edit-name">Collection Name</Label>
                                            <Input
                                                id="edit-name"
                                                value={editName}
                                                onChange={(e) => setEditName(e.target.value)}
                                                placeholder="Collection name"
                                            />
                                        </div>
                                        <Button
                                            onClick={() => updateMutation.mutate()}
                                            disabled={!editName || updateMutation.isPending}
                                            className="w-full"
                                        >
                                            {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
                                        </Button>
                                    </div>
                                </DialogContent>
                            </Dialog>
                        </div>

                        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'my' | 'shared')}>
                            <TabsList>
                                <TabsTrigger value="my">My Collections</TabsTrigger>
                                <TabsTrigger value="shared">Shared Collections</TabsTrigger>
                            </TabsList>
                            <TabsContent value="my" className="mt-6">
                                {isLoading ? (
                                    <div className="flex justify-center py-12">
                                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                                    </div>
                                ) : collections.length > 0 ? (
                                    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                                        {collections.map((collection: CollectionResponse) => (
                                            <Card 
                                                key={collection.id}
                                                className="shadow-custom-sm hover:shadow-custom-md transition-shadow cursor-pointer"
                                                onClick={() => handleCollectionClick(collection)}
                                            >
                                                <CardHeader>
                                                    <CardTitle className="flex items-start justify-between">
                                                        <span className="line-clamp-2 flex-1">{collection.name}</span>
                                                        <DropdownMenu>
                                                            <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                                                                <Button variant="ghost" size="icon" className="h-8 w-8">
                                                                    <MoreVertical className="h-4 w-4"/>
                                                                </Button>
                                                            </DropdownMenuTrigger>
                                                            <DropdownMenuContent align="end">
                                                                <DropdownMenuItem onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    handleEdit(collection);
                                                                }}>
                                                                    <Edit className="h-4 w-4 mr-2"/>
                                                                    Edit
                                                                </DropdownMenuItem>
                                                                <DropdownMenuItem 
                                                                    onClick={(e) => {
                                                                        e.stopPropagation();
                                                                        handleDelete(collection);
                                                                    }}
                                                                    className="text-destructive"
                                                                >
                                                                    <Trash2 className="h-4 w-4 mr-2"/>
                                                                    Delete
                                                                </DropdownMenuItem>
                                                            </DropdownMenuContent>
                                                        </DropdownMenu>
                                                    </CardTitle>
                                                </CardHeader>
                                                <CardContent>
                                                    <div className="text-sm text-muted-foreground space-y-1">
                                                        <p>Papers: {collection.paperCount || 0}</p>
                                                        <p>Members: {collection.memberCount || 0}</p>
                                                        {collection.currentUserAccessLevel && (
                                                            <p>Role: {collection.currentUserAccessLevel}</p>
                                                        )}
                                                    </div>
                                                </CardContent>
                                            </Card>
                                        ))}
                                    </div>
                                ) : (
                                    <Card className="text-center py-12">
                                        <CardContent>
                                            <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                                            <h3 className="text-lg font-semibold mb-2">No collections yet</h3>
                                            <p className="text-muted-foreground mb-4">
                                                Create your first collection to start collaborating with your team
                                            </p>
                                            <Button onClick={() => setIsCreateOpen(true)}>
                                                <Plus className="h-4 w-4 mr-2"/>
                                                Create Collection
                                            </Button>
                                        </CardContent>
                                    </Card>
                                )}
                            </TabsContent>
                            <TabsContent value="shared" className="mt-6">
                                {isLoading ? (
                                    <div className="flex justify-center py-12">
                                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                                    </div>
                                ) : collections.length > 0 ? (
                                    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                                        {collections.map((collection: CollectionResponse) => (
                                            <Card 
                                                key={collection.id}
                                                className="shadow-custom-sm hover:shadow-custom-md transition-shadow cursor-pointer"
                                                onClick={() => handleCollectionClick(collection)}
                                            >
                                                <CardHeader>
                                                    <CardTitle className="flex items-start justify-between">
                                                        <span className="line-clamp-2 flex-1">{collection.name}</span>
                                                        <Users className="h-5 w-5 text-muted-foreground flex-shrink-0 ml-2"/>
                                                    </CardTitle>
                                                </CardHeader>
                                                <CardContent>
                                                    <div className="text-sm text-muted-foreground space-y-1">
                                                        <p>Papers: {collection.paperCount || 0}</p>
                                                        <p>Members: {collection.memberCount || 0}</p>
                                                        {collection.currentUserAccessLevel && (
                                                            <p>Role: {collection.currentUserAccessLevel}</p>
                                                        )}
                                                        {collection.creatorName && (
                                                            <p>Created by: {collection.creatorName}</p>
                                                        )}
                                                    </div>
                                                </CardContent>
                                            </Card>
                                        ))}
                                    </div>
                                ) : (
                                    <Card className="text-center py-12">
                                        <CardContent>
                                            <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                                            <h3 className="text-lg font-semibold mb-2">No shared collections</h3>
                                            <p className="text-muted-foreground">
                                                Collections shared with you will appear here
                                            </p>
                                        </CardContent>
                                    </Card>
                                )}
                            </TabsContent>
                        </Tabs>

                    </div>
                </main>
            </div>
        </>
    );
};

export default Collections;
