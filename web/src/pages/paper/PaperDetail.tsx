import { useState, useMemo } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { supabase } from "@/integrations/supabase/client";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { ExternalLink, FileText } from "lucide-react";
import { toast } from "sonner";
import { useAuth } from "@/contexts/AuthContext";
import { Helmet } from "react-helmet-async";
import { getPaperDetails, addFavorite, removeFavorite } from "@/services/paper.service.ts";
import Header from "@/pages/Header.tsx";
import PaperDetailsHeader from "./components/PaperDetailsHeader";
import PaperDetailsMainContent from "./components/PaperDetailsMainContent";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import PDFViewer from "@/components/PDFViewer";
import { updateReadingProgress, createReadingWorkflow, getWorkflowsByUser } from "@/services/progress.service";
import { getMyCollections, createCollection } from "@/services/collection.service";

const PaperDetail = () => {
    const { id } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [isPDFViewerOpen, setIsPDFViewerOpen] = useState(false);

    const { data, isLoading } = useQuery({
        queryKey: ['paper', id, user?.id],
        queryFn: async () => await getPaperDetails(id!, user?.id),
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
            if (!id || !user?.id) throw new Error('Paper ID or User ID is missing');
            
            if (isFavorite) {
                await removeFavorite(id, user.id);
            } else {
                await addFavorite(id, user.id);
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['paper', id, user?.id] });
            queryClient.invalidateQueries({ queryKey: ['papers'] });
            toast.success(paper?.isFavorite ? 'Removed from favorites' : 'Added to favorites');
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to update favorite');
        },
    });

    // Get user's collections to find or create personal library
    const { data: myCollections } = useQuery({
        queryKey: ['collections', 'my', user?.id],
        queryFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await getMyCollections(user.id);
        },
        enabled: !!user,
    });

    // Get or create personal library collection
    const personalLibraryCollection = useMemo(() => {
        if (!myCollections?.content) return null;
        // Try to find "Personal Library" collection
        let collection = myCollections.content.find(c => c.name === 'Personal Library');
        // If not found, use first collection or create one
        if (!collection && myCollections.content.length > 0) {
            collection = myCollections.content[0];
        }
        return collection;
    }, [myCollections]);

    // Create personal library if needed
    const createPersonalLibraryMutation = useMutation({
        mutationFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            const result = await createCollection('Personal Library', user.id);
            // Invalidate collections query to refresh
            queryClient.invalidateQueries({ queryKey: ['collections', 'my', user.id] });
            return result;
        },
        onSuccess: () => {
            console.log('Personal Library created successfully');
        },
    });

    // Get collectionId - use personal library or first collection
    const collectionId = useMemo(() => {
        if (personalLibraryCollection) return personalLibraryCollection.id;
        // If no collections, create personal library immediately
        if (myCollections?.content && myCollections.content.length === 0 && user?.id && !createPersonalLibraryMutation.isPending) {
            createPersonalLibraryMutation.mutate();
        }
        return null;
    }, [personalLibraryCollection, myCollections, user?.id, createPersonalLibraryMutation]);

    const updateProgressMutation = useMutation({
        mutationFn: async ({ 
            pageNumber, 
            totalPages, 
            collectionId: providedCollectionId 
        }: { 
            pageNumber: number; 
            totalPages: number;
            collectionId?: string;
        }) => {
            const currentCollectionId = providedCollectionId || collectionId;
            
            if (!user?.id || !paper?.id || !currentCollectionId) {
                throw new Error('Missing required fields: user, paper, or collectionId');
            }

            // Calculate progress percentage
            const progress = totalPages > 0 ? Math.round((pageNumber / totalPages) * 100) : 0;

            console.log(`Updating progress: page ${pageNumber}/${totalPages} (${progress}%) for paper ${paper.id} in collection ${currentCollectionId}`);

            // Try to update existing workflow, if fails create new one
            try {
                await updateReadingProgress({
                    collectionId: currentCollectionId,
                    paperId: paper.id,
                    usersid: user.id,
                    lastPage: pageNumber,
                    progress: Math.min(100, Math.max(0, progress))
                });
                console.log('Progress updated successfully');
            } catch (error: any) {
                // If workflow doesn't exist, create it first
                if (error.message?.includes('not found') || error.message?.includes('404')) {
                    console.log('Workflow not found, creating new one...');
                    await createReadingWorkflow({
                        collectionId: currentCollectionId,
                        paperId: paper.id,
                        usersid: user.id
                    });
                    // Then update progress
                    await updateReadingProgress({
                        collectionId: currentCollectionId,
                        paperId: paper.id,
                        usersid: user.id,
                        lastPage: pageNumber,
                        progress: Math.min(100, Math.max(0, progress))
                    });
                    console.log('Workflow created and progress updated');
                } else {
                    console.error('Error updating progress:', error);
                    throw error;
                }
            }
        },
        onSuccess: () => {
            // Invalidate queries to refresh data
            queryClient.invalidateQueries({ queryKey: ['paper', id] });
            queryClient.invalidateQueries({ queryKey: ['papers'] });
            queryClient.invalidateQueries({ queryKey: ['workflows', user?.id] });
        },
    });

    const handleProgressUpdate = async (pageNumber: number, totalPages: number) => {
        // If no collectionId, try to get or create one first
        let currentCollectionId = collectionId;
        
        if (!currentCollectionId) {
            // Wait for collections to load
            if (!myCollections?.content) {
                console.warn('Collections not loaded yet, cannot save progress');
                return;
            }
            
            // Try to find Personal Library or use first collection
            let collection = myCollections.content.find(c => c.name === 'Personal Library');
            if (!collection && myCollections.content.length > 0) {
                collection = myCollections.content[0];
            }
            
            // If still no collection, create Personal Library
            if (!collection) {
                try {
                    if (!user?.id) {
                        console.error('User not logged in');
                        return;
                    }
                    const newCollection = await createCollection('Personal Library', user.id);
                    currentCollectionId = newCollection.id;
                    // Refresh collections query
                    queryClient.invalidateQueries({ queryKey: ['collections', 'my', user.id] });
                    console.log('Created Personal Library for progress tracking');
                } catch (error) {
                    console.error('Failed to create Personal Library:', error);
                    return;
                }
            } else {
                currentCollectionId = collection.id;
            }
        }
        
        if (currentCollectionId) {
            // Update mutation to use current collectionId
            updateProgressMutation.mutate({ 
                pageNumber, 
                totalPages,
                collectionId: currentCollectionId 
            });
        } else {
            console.error('Cannot save progress: no collectionId available');
        }
    };

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
    console.log('Paper details: ', paper);

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
                        <PaperDetailsMainContent
                            paper={paper}
                            toggleFavoriteMutation={toggleFavoriteMutation}
                            deleteMutation={deleteMutation}
                        />

                        <Tabs defaultValue="description" className="w-full">
                            <TabsList>
                                <TabsTrigger value="description">Description</TabsTrigger>
                                <TabsTrigger value="details">Details</TabsTrigger>
                                {/* <TabsTrigger value="notes">Notes</TabsTrigger> */}
                            </TabsList>

                            <TabsContent value="description" className="space-y-4">
                                <Card>
                                    <CardHeader>
                                        <CardTitle>Description</CardTitle>
                                    </CardHeader>
                                    <CardContent>
                                        <p className="text-sm leading-relaxed">
                                            {paper.description || 'No description available.'}
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

                                        {(paper.pdf_url || paper.dataUrl) && (
                                            <div>
                                                <h3 className="font-semibold mb-2">PDF</h3>
                                                <Button
                                                    variant="outline"
                                                    onClick={() => setIsPDFViewerOpen(true)}
                                                    className="inline-flex items-center gap-2"
                                                >
                                                    <FileText className="h-4 w-4" />
                                                    View PDF
                                                </Button>
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

            {/* PDF Viewer Dialog */}
            {(paper.pdf_url || paper.dataUrl) && (
                <PDFViewer
                    pdfUrl={paper.pdf_url || paper.dataUrl}
                    isOpen={isPDFViewerOpen}
                    onClose={() => setIsPDFViewerOpen(false)}
                    title={paper.title || "PDF Viewer"}
                    paperId={paper.id}
                    onProgressUpdate={handleProgressUpdate}
                />
            )}
        </>
    );
};

export default PaperDetail;
