import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {supabase} from "@/integrations/supabase/client";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Textarea} from "@/components/ui/textarea";
import {Plus, BookMarked} from "lucide-react";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import Header from "@/pages/Header.tsx";

const ReadingLists = () => {
    const {user} = useAuth();
    const queryClient = useQueryClient();
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [newList, setNewList] = useState({name: '', description: ''});

    const {data: readingLists, isLoading} = useQuery({
        queryKey: ['reading-lists', user?.id],
        queryFn: async () => {
            const {data, error} = await supabase
                .from('reading_lists')
                .select(`
          *,
          reading_list_papers(count)
        `)
                .eq('user_id', user?.id)
                .order('created_at', {ascending: false});

            if (error) throw error;
            return data;
        },
        enabled: !!user,
    });

    const createMutation = useMutation({
        mutationFn: async () => {
            const {error} = await supabase
                .from('reading_lists')
                .insert({
                    name: newList.name,
                    description: newList.description,
                    user_id: user?.id,
                });

            if (error) throw error;
        },
        onSuccess: () => {
            toast.success('Reading list created successfully');
            queryClient.invalidateQueries({queryKey: ['reading-lists']});
            setIsCreateOpen(false);
            setNewList({name: '', description: ''});
        },
        onError: () => {
            toast.error('Failed to create reading list');
        },
    });

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
                                {readingLists.map((list: any) => (
                                    <Card key={list.id}
                                          className="shadow-custom-sm hover:shadow-custom-md transition-shadow">
                                        <CardHeader>
                                            <CardTitle className="flex items-start justify-between">
                                                <span className="line-clamp-2">{list.name}</span>
                                                <BookMarked
                                                    className="h-5 w-5 text-muted-foreground flex-shrink-0 ml-2"/>
                                            </CardTitle>
                                            {list.description && (
                                                <CardDescription className="line-clamp-2">
                                                    {list.description}
                                                </CardDescription>
                                            )}
                                        </CardHeader>
                                        <CardContent>
                                            <div className="text-sm text-muted-foreground">
                                                <p>Papers: {list.reading_list_papers?.[0]?.count || 0}</p>
                                                <p className="text-xs mt-1">
                                                    Created {new Date(list.created_at).toLocaleDateString()}
                                                </p>
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
