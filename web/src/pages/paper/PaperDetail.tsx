import { useParams, Link, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ExternalLink } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import { Helmet } from "react-helmet-async";
import { getPaperDetails } from "@/services/paper.service.ts";
import Header from "@/pages/Header.tsx";
import PaperDetailsHeader from "./components/PaperDetailsHeader";
import TabContents from "./components/Tabs";

const PaperDetail = () => {
    const { id } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { data, isLoading } = useQuery({
        queryKey: ['paper', id],
        queryFn: async () => await getPaperDetails(id),
        enabled: !!user && !!id,
    });

    const paper = data?.data;

    const deleteMutation = useMutation({
        mutationFn: async () => {
            const { error } = await supabase
                .from('papers')
                .delete()
                .eq('id', id)
                .eq('user_id', user?.id);

            if (error) throw error;
        },
        onSuccess: () => {
            toast.success('Paper deleted successfully');
            queryClient.invalidateQueries({ queryKey: ['papers'] });
            navigate('/dashboard');
        },
        onError: () => {
            toast.error('Failed to delete paper');
        },
    });

    const toggleFavoriteMutation = useMutation({
        mutationFn: async (isFavorite: boolean) => {
            const { error } = await supabase
                .from('papers')
                .update({ is_favorite: !isFavorite })
                .eq('id', id)
                .eq('user_id', user?.id);

            if (error) throw error;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['paper', id] });
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
        paper?.description?.slice(0, 160) ||
        "View detailed information about the research paper, including journal, DOI, and related notes.";

    return (
        <>
            <Helmet>
                <title>{`${title} — LabVerse`}</title>
                <meta name="description" content={description} />
                <meta property="og:title" content={`${title} — LabVerse`} />
                <meta property="og:description" content={description} />
                <meta
                    property="og:image"
                    content="/og-paper.png"
                />
                <meta property="og:type" content="article" />
                <meta
                    property="og:url"
                    content={`https://labverse.app/papers/${paper.id || ""}`}
                />
                <meta property="og:site_name" content="LabVerse" />
                <link
                    rel="canonical"
                    href={`https://labverse.app/papers/${paper.id || ""}`}
                />
            </Helmet>
            <div className="min-h-screen bg-background">
                <Header />

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="max-w-4xl mx-auto space-y-6">
                        <div>
                            {/* Paper title */}
                            <PaperDetailsHeader
                                paper={paper}
                                toggleFavoriteMutation={toggleFavoriteMutation}
                                deleteMutation={deleteMutation}
                            />

                            {/* Badge */}
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
                                <p>
                                    <strong>Authors:</strong>
                                    {paper.authors?.join(', ')}
                                </p>
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
                                            <ExternalLink className="h-3 w-3" />
                                        </a>
                                    </p>
                                )}
                            </div>
                        </div>

                        <TabContents paper={paper} />
                    </div>
                </main>
            </div>
        </>
    );
};

export default PaperDetail;
