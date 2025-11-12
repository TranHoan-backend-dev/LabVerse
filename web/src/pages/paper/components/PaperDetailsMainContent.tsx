import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList } from '@/components/ui/tabs';
import { TabsTrigger } from '@radix-ui/react-tabs';
import { ExternalLink, FileText } from 'lucide-react';
import PaperDetailsHeader from './PaperDetailsHeader';

interface TabContentsProps {
    paper: {
        authors: string;
        journal?: string;
        year?: string;
        doi?: string;
        title: string;
        status: string;
        priority: string;
        id: string;
        is_favorite: boolean;
    };
    toggleFavoriteMutation: {
        mutate: (is_favorite: boolean) => void;
    };
    deleteMutation: {
        mutate: () => void;
    }
}

const PaperDetailsMainContent = ({
    paper,
    toggleFavoriteMutation,
    deleteMutation
}: TabContentsProps) => {
    return (
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
                    <strong>Authors: </strong>
                    {paper.authors}
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
    );
};

export default PaperDetailsMainContent;