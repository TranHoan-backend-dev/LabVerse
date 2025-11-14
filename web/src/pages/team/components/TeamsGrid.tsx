import React from 'react';
import {Card, CardContent} from '@/components/ui/card';
import {Button} from '@/components/ui/button';
import {Plus, Users} from 'lucide-react';
import TeamCard from './TeamCard';
import { TeamResponse } from '@/types/team.types';

type Props = {
    teams: TeamResponse[];
    isLoading: boolean;
    onDelete: (team: TeamResponse) => void;
    onClick: (team: TeamResponse) => void;
    onCreateClick?: () => void;
};

const TeamsGrid: React.FC<Props> = ({
    teams,
    isLoading,
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

    if (teams.length === 0) {
        return (
            <Card className="text-center py-12">
                <CardContent>
                    <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                    <h3 className="text-lg font-semibold mb-2">No teams found</h3>
                    <p className="text-muted-foreground mb-4">
                        Try adjusting your search or filters
                    </p>
                    {onCreateClick && (
                        <Button onClick={onCreateClick}>
                            <Plus className="h-4 w-4 mr-2"/>
                            Create Team
                        </Button>
                    )}
                </CardContent>
            </Card>
        );
    }

    return (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {teams.map((team) => (
                <TeamCard
                    key={team.id}
                    team={team}
                    onDelete={onDelete}
                    onClick={onClick}
                />
            ))}
        </div>
    );
};

export default TeamsGrid;
