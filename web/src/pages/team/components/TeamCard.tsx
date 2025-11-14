import React from 'react';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from '@/components/ui/card';
import {Button} from '@/components/ui/button';
import {Globe, Lock, MoreVertical, Trash2} from 'lucide-react';
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { TeamResponse } from '@/types/team.types';

type Props = {
    team: TeamResponse;
    onDelete: (team: TeamResponse) => void;
    onClick: (team: TeamResponse) => void;
};

const TeamCard: React.FC<Props> = ({team, onDelete, onClick}) => {
    const handleStopPropagation = (e: React.MouseEvent | React.PointerEvent) => {
        e.stopPropagation();
    };

    return (
        <Card
            className="shadow-custom-sm hover:shadow-custom-md transition-shadow cursor-pointer"
            onClick={() => onClick(team)}
        >
            <CardHeader>
                <CardTitle className="flex items-start justify-between">
                    <span className="line-clamp-2 flex-1">{team.name}</span>
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
                                    onDelete(team);
                                }}
                                className="text-destructive"
                            >
                                <Trash2 className="h-4 w-4 mr-2"/>
                                Delete
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </CardTitle>
                {team.description && (
                    <CardDescription className="line-clamp-2">
                        {team.description}
                    </CardDescription>
                )}
            </CardHeader>
            <CardContent>
                <div className="text-sm text-muted-foreground space-y-2">
                    <div className="flex items-center gap-2">
                        {team.privacy === 'PUBLIC' ? (
                            <Globe className="h-4 w-4"/>
                        ) : (
                            <Lock className="h-4 w-4"/>
                        )}
                        <span>{team.privacy}</span>
                    </div>
                    {team.researchField && (
                        <p>Field: {team.researchField}</p>
                    )}
                    <p>Members: {team.memberCount || 0}</p>
                    <p>Papers: {team.paperCount || 0}</p>
                </div>
            </CardContent>
        </Card>
    );
};

export default TeamCard;
