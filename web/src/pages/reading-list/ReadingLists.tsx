import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import Header from "@/components/Header";
import {
    createReadingList,
    getReadingListsByUser,
    deleteReadingList,
    type ReadingListResponse
} from "@/services/reading-list.service";
import CreateReadingListDialog from "./components/CreateReadingListDialog";
import ReadingListsGrid from "./components/ReadingListsGrid";

const ReadingLists = () => {
    const {user} = useAuth();
    const queryClient = useQueryClient();
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [newList, setNewList] = useState({name: '', description: ''});

    const {data: readingLists, isLoading} = useQuery({
        queryKey: ['reading-lists', user?.id],
        queryFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await getReadingListsByUser(user.id);
        },
        enabled: !!user,
    });

    const createMutation = useMutation({
        mutationFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await createReadingList({
                name: newList.name,
                description: newList.description || undefined,
                userId: user.id
            });
        },
        onSuccess: () => {
            toast.success('Reading list created successfully');
            queryClient.invalidateQueries({queryKey: ['reading-lists']});
            setIsCreateOpen(false);
            setNewList({name: '', description: ''});
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to create reading list');
        },
    });

    const deleteMutation = useMutation({
        mutationFn: async (listId: string) => {
            return await deleteReadingList(listId);
        },
        onSuccess: () => {
            toast.success('Reading list deleted successfully');
            queryClient.invalidateQueries({queryKey: ['reading-lists']});
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to delete reading list');
        },
    });

    const handleDelete = (list: ReadingListResponse) => {
        if (window.confirm(`Are you sure you want to delete "${list.name}"? This action cannot be undone.`)) {
            deleteMutation.mutate(list.id);
        }
    };

    return (
        <>
            <Helmet>
                <title>Reading Lists | LabVerse</title>
                <meta
                    name="description"
                    content="Create and organize your research papers into themed reading lists with LabVerse. Manage your academic resources effectively."
                />
                <meta
                    name="keywords"
                    content="LabVerse, reading lists, research papers, academic organization, collections, study management"
                />
                <meta name="author" content="LabVerse Team"/>
                <meta property="og:title" content="Reading Lists | LabVerse"/>
                <meta
                    property="og:description"
                    content="Organize and manage your research papers with customizable reading lists in LabVerse."
                />
                <meta property="og:type" content="website"/>
                <meta property="og:url" content="https://labverse.app/dashboard/reading-lists"/>
                <meta property="og:image" content="https://labverse.app/og-image.png"/>
                <meta name="twitter:card" content="summary_large_image"/>
                <meta name="twitter:title" content="Reading Lists | LabVerse"/>
                <meta
                    name="twitter:description"
                    content="Easily create, view, and organize your academic reading lists with LabVerse."
                />
            </Helmet>
            <div className="min-h-screen bg-background">
                <Header/>

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="space-y-8">
                        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                            <div>
                                <h1 className="text-3xl font-bold mb-2">Reading Lists</h1>
                                <p className="text-muted-foreground">
                                    Organize papers into themed collections
                                </p>
                            </div>

                            <CreateReadingListDialog
                                open={isCreateOpen}
                                onOpenChange={setIsCreateOpen}
                                listName={newList.name}
                                description={newList.description}
                                onNameChange={(name) => setNewList({...newList, name})}
                                onDescriptionChange={(desc) => setNewList({...newList, description: desc})}
                                onSubmit={() => createMutation.mutate()}
                                isLoading={createMutation.status === 'pending'}
                            />
                        </div>

                        <ReadingListsGrid
                            readingLists={readingLists || []}
                            isLoading={isLoading}
                            onDelete={handleDelete}
                            onCreateClick={() => setIsCreateOpen(true)}
                        />
                    </div>
                </main>
            </div>
        </>
    );
};

export default ReadingLists;
