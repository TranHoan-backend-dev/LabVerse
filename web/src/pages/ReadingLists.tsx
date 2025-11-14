import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Textarea} from "@/components/ui/textarea";
import {Plus, BookMarked, Trash2, MoreVertical} from "lucide-react";
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
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

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

                            <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
                                <DialogTrigger asChild>
                                    <Button size="lg" className="sm:w-auto w-full">
                                        <Plus className="h-5 w-5 mr-2"/>
                                        Create List
                                    </Button>
                                </DialogTrigger>
                                <DialogContent>
                                    <DialogHeader>
                                        <DialogTitle>Create Reading List</DialogTitle>
                                    </DialogHeader>
                                    <div className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="name">List Name</Label>
                                            <Input
                                                id="name"
                                                value={newList.name}
                                                onChange={(e) => setNewList({...newList, name: e.target.value})}
                                                placeholder="e.g., Papers to Review This Week"
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="description">Description</Label>
                                            <Textarea
                                                id="description"
                                                value={newList.description}
                                                onChange={(e) => setNewList({...newList, description: e.target.value})}
                                                placeholder="What's this list for?"
                                                rows={3}
                                            />
                                        </div>
                                        <Button
                                            onClick={() => createMutation.mutate()}
                                            disabled={!newList.name || createMutation.isPending}
                                            className="w-full"
                                        >
                                            {createMutation.isPending ? 'Creating...' : 'Create List'}
                                        </Button>
                                    </div>
                                </DialogContent>
                            </Dialog>
                        </div>

                        {isLoading ? (
                            <div className="flex justify-center py-12">
                                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                            </div>
                        ) : readingLists && readingLists.length > 0 ? (
                            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                                {readingLists.map((list: ReadingListResponse) => (
                                    <Card key={list.id}
                                          className="shadow-custom-sm hover:shadow-custom-md transition-shadow">
                                        <CardHeader>
                                            <CardTitle className="flex items-start justify-between">
                                                <span className="line-clamp-2 flex-1">{list.name}</span>
                                                <DropdownMenu>
                                                    <DropdownMenuTrigger asChild>
                                                        <Button variant="ghost" size="icon" className="h-8 w-8">
                                                            <MoreVertical className="h-4 w-4"/>
                                                        </Button>
                                                    </DropdownMenuTrigger>
                                                    <DropdownMenuContent align="end">
                                                        <DropdownMenuItem 
                                                            onClick={() => handleDelete(list)}
                                                            className="text-destructive"
                                                        >
                                                            <Trash2 className="h-4 w-4 mr-2"/>
                                                            Delete
                                                        </DropdownMenuItem>
                                                    </DropdownMenuContent>
                                                </DropdownMenu>
                                            </CardTitle>
                                            {list.description && (
                                                <CardDescription className="line-clamp-2">
                                                    {list.description}
                                                </CardDescription>
                                            )}
                                        </CardHeader>
                                        <CardContent>
                                            <div className="text-sm text-muted-foreground">
                                                <p>Papers: {list.paperIds?.length || 0}</p>
                                                <p>Members: {list.userIds?.length || 0}</p>
                                                {list.createdAt && (
                                                    <p className="text-xs mt-1">
                                                        Created {new Date(list.createdAt).toLocaleDateString()}
                                                    </p>
                                                )}
                                            </div>
                                        </CardContent>
                                    </Card>
                                ))}
                            </div>
                        ) : (
                            <Card className="text-center py-12">
                                <CardContent>
                                    <BookMarked className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                                    <h3 className="text-lg font-semibold mb-2">No reading lists yet</h3>
                                    <p className="text-muted-foreground mb-4">
                                        Create your first reading list to organize papers by theme or project
                                    </p>
                                    <Button onClick={() => setIsCreateOpen(true)}>
                                        <Plus className="h-4 w-4 mr-2"/>
                                        Create List
                                    </Button>
                                </CardContent>
                            </Card>
                        )}
                    </div>
                </main>
            </div>
        </>
    );
};

export default ReadingLists;
