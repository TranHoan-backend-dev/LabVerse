import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList } from '@/components/ui/tabs';
import { TabsTrigger } from '@radix-ui/react-tabs';
import { FileText } from 'lucide-react';

interface TabContentsProps {
    paper: {
        description: string;
        keywords: string[];
        pdf_url?: string;
        total_pages?: number;
        last_read_page?: number;
    };
}

const TabContents = ({
    paper
}: TabContentsProps) => {
    return (
        <Tabs defaultValue="description" className="w-full">
            <TabsList>
                <TabsTrigger value="description">Description</TabsTrigger>
                <TabsTrigger value="details">Details</TabsTrigger>
                <TabsTrigger value="notes">Notes</TabsTrigger>
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

                        {paper.pdf_url && (
                            <div>
                                <h3 className="font-semibold mb-2">PDF</h3>
                                <a
                                    href={paper.pdf_url}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                    className="inline-flex items-center gap-2 text-primary hover:underline"
                                >
                                    <FileText className="h-4 w-4" />
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
    );
};

export default TabContents;