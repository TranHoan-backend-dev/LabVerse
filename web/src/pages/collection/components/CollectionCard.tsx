import React from 'react';
import {Card, CardHeader, CardTitle, CardContent} from '@/components/ui/card';
import {Button} from '@/components/ui/button';
import {Users, MoreVertical, Edit, Trash2} from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type { CollectionResponse } from '@/services/collection.service';

type Props = {
    collection: CollectionResponse;
    isShared?: boolean;
    onEdit: (collection: CollectionResponse) => void;
    onDelete: (collection: CollectionResponse) => void;
    onClick: (collection: CollectionResponse) => void;
};

const CollectionCard: React.FC<Props> = ({collection, isShared = false, onEdit, onDelete, onClick}) => {
    const handleStopPropagation = (e: React.MouseEvent) => {
        e.stopPropagation();
    };

    return (
        <Card 
            className="shadow-custom-sm hover:shadow-custom-md transition-shadow cursor-pointer"
            onClick={() => onClick(collection)}
        >
            <CardHeader>
                <CardTitle className="flex items-start justify-between">
                    <span className="line-clamp-2 flex-1">{collection.name}</span>
                    {!isShared && (
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild onClick={handleStopPropagation}>
                                <Button variant="ghost" size="icon" className="h-8 w-8">
                                    <MoreVertical className="h-4 w-4"/>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                                <DropdownMenuItem onClick={(e: React.MouseEvent<HTMLDivElement>) => {
                                    handleStopPropagation(e);
                                    onEdit(collection);
                                }}>
                                    <Edit className="h-4 w-4 mr-2"/>
                                    Edit
                                </DropdownMenuItem>
                                <DropdownMenuItem 
                                    onClick={(e: React.MouseEvent<HTMLDivElement>) => {
                                        handleStopPropagation(e);
                                        onDelete(collection);
                                    }}
                                    className="text-destructive"
                                >
                                    <Trash2 className="h-4 w-4 mr-2"/>
                                    Delete
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    )}
                    {isShared && (
                        <Users className="h-5 w-5 text-muted-foreground flex-shrink-0 ml-2"/>
                    )}
                </CardTitle>
            </CardHeader>
            <CardContent>
                <div className="text-sm text-muted-foreground space-y-1">
                    <p>Papers: {collection.paperCount || 0}</p>
                    <p>Members: {collection.memberCount || 0}</p>
                    {collection.currentUserAccessLevel && (
                        <p>Role: {collection.currentUserAccessLevel}</p>
                    )}
                    {isShared && collection.creatorName && (
                        <p>Created by: {collection.creatorName}</p>
                    )}
                </div>
            </CardContent>
        </Card>
    );
};

export default CollectionCard;
