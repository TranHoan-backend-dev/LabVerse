import React from 'react';
import {Card, CardContent} from '@/components/ui/card';
import {Button} from '@/components/ui/button';
import {Plus, Users} from 'lucide-react';
import CollectionCard from './CollectionCard';
import type { CollectionResponse } from '@/services/collection.service';

type Props = {
    collections: CollectionResponse[];
    isLoading: boolean;
    isShared?: boolean;
    onEdit: (collection: CollectionResponse) => void;
    onDelete: (collection: CollectionResponse) => void;
    onClick: (collection: CollectionResponse) => void;
    onCreateClick?: () => void;
};

const CollectionsGrid: React.FC<Props> = ({
    collections,
    isLoading,
    isShared = false,
    onEdit,
    onDelete,
    onClick,
    onCreateClick,
}) => {
    if (isLoading) {
        return (
            <div className="flex justify-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
        );
    }

    if (collections.length === 0) {
        return (
            <Card className="text-center py-12">
                <CardContent>
                    <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                    <h3 className="text-lg font-semibold mb-2">
                        {isShared ? 'No shared collections' : 'No collections yet'}
                    </h3>
                    <p className="text-muted-foreground mb-4">
                        {isShared 
                            ? 'Collections shared with you will appear here'
                            : 'Create your first collection to start collaborating with your team'
                        }
                    </p>
                    {!isShared && onCreateClick && (
                        <Button onClick={onCreateClick}>
                            <Plus className="h-4 w-4 mr-2"/>
                            Create Collection
                        </Button>
                    )}
                </CardContent>
            </Card>
        );
    }

    return (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {collections.map((collection) => (
                <CollectionCard
                    key={collection.id}
                    collection={collection}
                    isShared={isShared}
                    onEdit={onEdit}
                    onDelete={onDelete}
                    onClick={onClick}
                />
            ))}
        </div>
    );
};

export default CollectionsGrid;
