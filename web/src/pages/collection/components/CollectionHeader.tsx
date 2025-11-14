import React from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { ArrowLeft } from 'lucide-react';
import type { CollectionResponse } from '@/services/collection.service';

type Props = {
    collection: CollectionResponse;
    onBack: () => void;
};

const CollectionHeader: React.FC<Props> = ({ collection, onBack }) => {
    return (
        <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={onBack}>
                <ArrowLeft className="h-5 w-5" />
            </Button>
            <div className="flex-1">
                <h1 className="text-3xl font-bold">{collection.name}</h1>
                <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                    <span>Papers: {collection.paperCount || 0}</span>
                    <span>Members: {collection.memberCount || 0}</span>
                    {collection.currentUserAccessLevel && (
                        <Badge variant="outline">{collection.currentUserAccessLevel}</Badge>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CollectionHeader;
