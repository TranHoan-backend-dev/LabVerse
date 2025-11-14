import { Button } from '@/components/ui/button';
import { Star, Trash2 } from 'lucide-react';

interface PaperDetailsHeaderProps {
    paper: {
        id: string;
        title: string;
        isFavorite: boolean;
    };
    toggleFavoriteMutation: {
        mutate: (isFavorite: boolean) => void;
    };
    deleteMutation: {
        mutate: () => void;
    };
}

const PaperDetailsHeader = ({
    paper,
    toggleFavoriteMutation,
    deleteMutation
}: PaperDetailsHeaderProps) => {
    return (
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-4">
            <h1 className="text-3xl font-bold">{paper.title}</h1>

            {/* Action buttons */}
            <div className="flex items-center gap-2">
                <Button
                    variant="ghost"
                    size="icon"
                    onClick={() => toggleFavoriteMutation.mutate(paper.isFavorite)}
                >
                    <Star
                        className={`h-5 w-5 ${paper.isFavorite ? 'fill-yellow-400 text-yellow-400' : ''}`}
                    />
                </Button>
                <Button
                    variant="destructive"
                    size="icon"
                    onClick={() => deleteMutation.mutate()}
                >
                    <Trash2 className="h-5 w-5" />
                </Button>
            </div>
        </div>
    );
};

export default PaperDetailsHeader;