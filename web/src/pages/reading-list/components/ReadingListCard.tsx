import React from 'react';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card';
import {Button} from '@/components/ui/button';
import {Trash2, MoreVertical} from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import type { ReadingListResponse } from '@/services/reading-list.service';

type Props = {
    list: ReadingListResponse;
    onDelete: (list: ReadingListResponse) => void;
};

const ReadingListCard: React.FC<Props> = ({list, onDelete}) => {
    const handleStopPropagation = (e: React.MouseEvent | React.PointerEvent) => {
        e.stopPropagation();
    };

    return (
        <Card className="shadow-custom-sm hover:shadow-custom-md transition-shadow">
            <CardHeader>
                <CardTitle className="flex items-start justify-between">
                    <span className="line-clamp-2 flex-1">{list.name}</span>
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button 
                                variant="ghost" 
                                size="icon" 
                                className="h-8 w-8"
                                onClick={handleStopPropagation}
                            >
                                <MoreVertical className="h-4 w-4"/>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem 
                                onClick={(e) => {
                                    e.stopPropagation();
                                    onDelete(list);
                                }}
                                className="text-destructive"
                            >
                                <Trash2 className="h-4 w-4 mr-2"/>
                                Delete
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </CardTitle>
                {list.description && (
                    <CardDescription className="line-clamp-2">
                        {list.description}
                    </CardDescription>
                )}
            </CardHeader>
            <CardContent>
                <div className="text-sm text-muted-foreground">
                    <p>Papers: {list.paperIds?.length || 0}</p>
                    <p>Members: {list.userIds?.length || 0}</p>
                    {list.createdAt && (
                        <p className="text-xs mt-1">
                            Created {new Date(list.createdAt).toLocaleDateString()}
                        </p>
                    )}
                </div>
            </CardContent>
        </Card>
    );
};

export default ReadingListCard;
