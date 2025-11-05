import {useParams, Link, useNavigate} from "react-router-dom";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {supabase} from "@/integrations/supabase/client";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Badge} from "@/components/ui/badge";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {ArrowLeft, BookOpen, Star, Trash2, ExternalLink, FileText} from "lucide-react";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";

const PaperDetail = () => {
    const {id} = useParams();
    const {user} = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const {data: paper, isLoading} = useQuery({
        queryKey: ['paper', id],
        queryFn: async () => {
            const {data, error} = await supabase
                .from('papers')
                .select('*')
                .eq('id', id)
                .eq('user_id', user?.id)
                .single();

            if (error) throw error;
            return data;
        },
        enabled: !!user && !!id,
    });

    const deleteMutation = useMutation({
        mutationFn: async () => {
            const {error} = await supabase
                .from('papers')
                .delete()
                .eq('id', id)
                .eq('user_id', user?.id);

            if (error) throw error;
        },
        onSuccess: () => {
            toast.success('Paper deleted successfully');
            queryClient.invalidateQueries({queryKey: ['papers']});
            navigate('/dashboard');
        },
        onError: () => {
            toast.error('Failed to delete paper');
        },
    });

    const toggleFavoriteMutation = useMutation({
        mutationFn: async (isFavorite: boolean) => {
            const {error} = await supabase
                .from('papers')
                .update({is_favorite: !isFavorite})
                .eq('id', id)
                .eq('user_id', user?.id);

            if (error) throw error;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['paper', id]});
            toast.success(paper?.is_favorite ? 'Removed from favorites' : 'Added to favorites');
        },
    });

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
        );
    }

    if (!paper) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <h2 className="text-2xl font-bold mb-2">Paper not found</h2>
                    <Link to="/dashboard">
                        <Button>Back to Dashboard</Button>
                    </Link>
                </div>
            </div>
        );
    }

    const title = paper?.title || "Research Paper";
    const description =
        paper?.abstract?.slice(0, 160) ||
        "View detailed information about the research paper, including journal, DOI, and related notes.";

    return (
        <>
            <Helmet>
                <title>{`${title} — LabVerse`}</title>
                <meta name="description" content={description}/>
                <meta property="og:title" content={`${title} — LabVerse`}/>
                <meta property="og:description" content={description}/>
                <meta
                    property="og:image"
                    content="/og-paper.png"
                />
                <meta property="og:type" content="article"/>
                <meta
                    property="og:url"
                    content={`https://labverse.app/papers/${paper.id || ""}`}
                />
                <meta property="og:site_name" content="LabVerse"/>
                <link
                    rel="canonical"
                    href={`https://labverse.app/papers/${paper.id || ""}`}
                />
            </Helmet>
            <div className="min-h-screen bg-background">
                <header className="border-b border-border bg-background/80 backdrop-blur-sm sticky top-0 z-50">
                    <div className="container mx-auto px-4 sm:px-6 lg:px-8">
                        <div className="flex h-16 items-center justify-between">
                            <Link to="/dashboard"
                                  className="flex items-center gap-2 transition-smooth hover:opacity-80">
                                <ArrowLeft className="h-5 w-5"/>
                                <BookOpen className="h-6 w-6 text-primary"/>
                                <span className="text-xl font-bold text-gradient">LabVerse</span>
                            </Link>

                            <div className="flex items-center gap-2">
                                <Button
                                    variant="ghost"
                                    size="icon"
                                    onClick={() => toggleFavoriteMutation.mutate(paper.is_favorite)}
                                >
                                    <Star
                                        className={`h-5 w-5 ${paper.is_favorite ? 'fill-yellow-400 text-yellow-400' : ''}`}/>
                                </Button>
                                <Button
                                    variant="destructive"
                                    size="icon"
                                    onClick={() => deleteMutation.mutate()}
                                >
                                    <Trash2 className="h-5 w-5"/>
                                </Button>
                            </div>
                        </div>
                    </div>
                </header>

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="max-w-4xl mx-auto space-y-6">
                        <div>
                            <h1 className="text-3xl font-bold mb-4">{paper.title}</h1>
                            <div className="flex flex-wrap gap-2 mb-4">
                                <Badge variant={paper.status === 'Finished' ? 'default' : 'secondary'}>
                                    {paper.status}
                                </Badge>
                                <Badge
                                    variant={paper.priority === 'High' ? 'destructive' : paper.priority === 'Medium' ? 'default' : 'outline'}>
                                    {paper.priority} Priority
                                </Badge>
                            </div>
                            <div className="text-sm text-muted-foreground space-y-1">
                                <p><strong>Authors:</strong> {paper.authors?.join(', ')}</p>
                                {paper.journal && <p><strong>Journal:</strong> {paper.journal}</p>}
                                {paper.year && <p><strong>Year:</strong> {paper.year}</p>}
                                {paper.doi && (
                                    <p className="flex items-center gap-2">
                                        <strong>DOI:</strong>
                                        <a
                                            href={`https://doi.org/${paper.doi}`}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="text-primary hover:underline inline-flex items-center gap-1"
                                        >
                                            {paper.doi}
                                            <ExternalLink className="h-3 w-3"/>
                                        </a>
                                    </p>
                                )}
                            </div>
                        </div>

                        <Tabs defaultValue="abstract" className="w-full">
                            <TabsList>
                                <TabsTrigger value="abstract">Abstract</TabsTrigger>
                                <TabsTrigger value="details">Details</TabsTrigger>
                                <TabsTrigger value="notes">Notes</TabsTrigger>
                            </TabsList>

                            <TabsContent value="abstract" className="space-y-4">
                                <Card>
                                    <CardHeader>
                                        <CardTitle>Abstract</CardTitle>
                                    </CardHeader>
                                    <CardContent>
                                        <p className="text-sm leading-relaxed">
                                            {paper.abstract || 'No abstract available.'}
                                        </p>
                                    </CardContent>
                                </Card>
                            </TabsContent>

                            <TabsContent value="details" className="space-y-4">
                                <Card>
                                    <CardHeader>
                                        <CardTitle>Paper Details</CardTitle>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        {paper.keywords && paper.keywords.length > 0 && (
                                            <div>
                                                <h3 className="font-semibold mb-2">Keywords</h3>
                                                <div className="flex flex-wrap gap-2">
                                                    {paper.keywords.map((keyword, index) => (
                                                        <Badge key={index} variant="outline">{keyword}</Badge>
                                                    ))}
                                                </div>
                                            </div>
                                        )}

                                        {paper.pdf_url && (
                                            <div>
                                                <h3 className="font-semibold mb-2">PDF</h3>
                                                <a
                                                    href={paper.pdf_url}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="inline-flex items-center gap-2 text-primary hover:underline"
                                                >
                                                    <FileText className="h-4 w-4"/>
                                                    View PDF
                                                </a>
                                            </div>
                                        )}

                                        {paper.total_pages && (
                                            <div>
                                                <h3 className="font-semibold mb-2">Reading Progress</h3>
                                                <p className="text-sm">
                                                    Page {paper.last_read_page} of {paper.total_pages}
                                                    {' '}({Math.round((paper.last_read_page / paper.total_pages) * 100)}%)
                                                </p>
                                            </div>
                                        )}
                                    </CardContent>
                                </Card>
                            </TabsContent>

                            <TabsContent value="notes" className="space-y-4">
                                <Card>
                                    <CardHeader>
                                        <CardTitle>Notes & Annotations</CardTitle>
                                    </CardHeader>
                                    <CardContent>
                                        <p className="text-sm text-muted-foreground">
                                            Annotations feature coming soon. You'll be able to add highlights and notes
                                            to your papers.
                                        </p>
                                    </CardContent>
                                </Card>
                            </TabsContent>
                        </Tabs>
                    </div>
                </main>
            </div>
        </>
    );
};

export default PaperDetail;
