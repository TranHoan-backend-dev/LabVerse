import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {useNavigate} from "react-router-dom";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import Header from "@/pages/Header.tsx";
import {
    createCollection,
    getMyCollections,
    getSharedCollections,
    updateCollection,
    deleteCollection,
    type CollectionResponse
} from "@/services/collection.service";
import CreateCollectionDialog from "./components/CreateCollectionDialog";
import EditCollectionDialog from "./components/EditCollectionDialog";
import CollectionsGrid from "./components/CollectionsGrid";

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

                            <CreateCollectionDialog
                                open={isCreateOpen}
                                onOpenChange={setIsCreateOpen}
                                collectionName={newCollection.name}
                                onNameChange={(name) => setNewCollection({...newCollection, name})}
                                onSubmit={() => createMutation.mutate()}
                                isLoading={createMutation.status === 'pending'}
                            />

                            <EditCollectionDialog
                                open={isEditOpen}
                                onOpenChange={setIsEditOpen}
                                collectionName={editName}
                                onNameChange={setEditName}
                                onSubmit={() => updateMutation.mutate()}
                                isLoading={updateMutation.status === 'pending'}
                            />
                        </div>

                        <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'my' | 'shared')}>
                            <TabsList>
                                <TabsTrigger value="my">My Collections</TabsTrigger>
                                <TabsTrigger value="shared">Shared Collections</TabsTrigger>
                            </TabsList>
                            <TabsContent value="my" className="mt-6">
                                <CollectionsGrid
                                    collections={collections}
                                    isLoading={isLoading}
                                    isShared={false}
                                    onEdit={handleEdit}
                                    onDelete={handleDelete}
                                    onClick={handleCollectionClick}
                                    onCreateClick={() => setIsCreateOpen(true)}
                                />
                            </TabsContent>
                            <TabsContent value="shared" className="mt-6">
                                <CollectionsGrid
                                    collections={collections}
                                    isLoading={isLoading}
                                    isShared={true}
                                    onEdit={handleEdit}
                                    onDelete={handleDelete}
                                    onClick={handleCollectionClick}
                                    onCreateClick={() => setIsCreateOpen(true)}
                                />
                            </TabsContent>
                        </Tabs>

                    </div>
                </main>
            </div>
        </>
    );
};

export default Collections;
