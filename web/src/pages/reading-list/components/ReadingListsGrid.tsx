import React from 'react';
import {Card, CardContent} from '@/components/ui/card';
import {Button} from '@/components/ui/button';
import {Plus, BookMarked} from 'lucide-react';
import ReadingListCard from './ReadingListCard';
import type { ReadingListResponse } from '@/services/reading-list.service';

type Props = {
    readingLists: ReadingListResponse[];
    isLoading: boolean;
    onDelete: (list: ReadingListResponse) => void;
    onCreateClick?: () => void;
};

const ReadingListsGrid: React.FC<Props> = ({
    readingLists,
    isLoading,
    onDelete,
    onCreateClick,
}) => {
    if (isLoading) {
        return (
            <div className="flex justify-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
        );
    }

    if (readingLists.length === 0) {
        return (
            <Card className="text-center py-12">
                <CardContent>
                    <BookMarked className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                    <h3 className="text-lg font-semibold mb-2">No reading lists yet</h3>
                    <p className="text-muted-foreground mb-4">
                        Create your first reading list to organize papers by theme or project
                    </p>
                    {onCreateClick && (
                        <Button onClick={onCreateClick}>
                            <Plus className="h-4 w-4 mr-2"/>
                            Create List
                        </Button>
                    )}
                </CardContent>
            </Card>
        );
    }

    return (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {readingLists.map((list) => (
                <ReadingListCard
                    key={list.id}
                    list={list}
                    onDelete={onDelete}
                />
            ))}
        </div>
    );
};

export default ReadingListsGrid;
