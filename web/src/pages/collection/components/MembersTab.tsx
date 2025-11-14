import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';
import { Trash2, Users } from 'lucide-react';
import type { CollectionUserResponse } from '@/services/collection.service';

type Props = {
    isLoadingMembers: boolean;
    members: CollectionUserResponse[] | undefined;
    canManageMembers: boolean;
    currentUserId?: string;
    handleRemoveMember: (m: CollectionUserResponse) => void;
};

const MembersTab: React.FC<Props> = ({
    isLoadingMembers,
    members,
    canManageMembers,
    currentUserId,
    handleRemoveMember,
}) => {
    return (
        <>
            {isLoadingMembers ? (
                <div className="flex justify-center py-12">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                </div>
            ) : members && members.length > 0 ? (
                <div className="grid gap-4">
                    {members.map((member) => (
                        <Card key={member.memberId} className="hover:shadow-md transition-shadow">
                            <CardHeader>
                                <div className="flex items-start justify-between">
                                    <div className="flex items-start gap-3 flex-1">
                                        <Avatar className="h-12 w-12 flex-shrink-0">
                                            <AvatarImage 
                                                src={member.memberAvatarUrl} 
                                                alt={member.memberName}
                                            />
                                            <AvatarFallback>
                                                {member.memberName?.charAt(0)?.toUpperCase() || 'U'}
                                            </AvatarFallback>
                                        </Avatar>
                                        <div className="flex-1 min-w-0">
                                            <CardTitle className="text-lg font-semibold mb-1">
                                                {member.memberName}
                                            </CardTitle>
                                            <p className="text-xs text-muted-foreground mb-3">
                                                {member.memberEmail}
                                            </p>
                                            <div className="flex gap-2">
                                                <Badge variant="outline">{member.accessLevel}</Badge>
                                                <Badge variant="secondary">{member.role}</Badge>
                                            </div>
                                        </div>
                                    </div>
                                    {canManageMembers && member.memberId !== currentUserId && (
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => handleRemoveMember(member)}
                                            className="flex-shrink-0"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    )}
                                </div>
                            </CardHeader>
                        </Card>
                    ))}
                </div>
            ) : (
                <Card className="text-center py-12">
                    <CardContent>
                        <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                        <h3 className="text-lg font-semibold mb-2">No members yet</h3>
                        <p className="text-muted-foreground">
                            {canManageMembers
                                ? 'Add members to this collection to start collaborating'
                                : 'No members have been added to this collection'}
                        </p>
                    </CardContent>
                </Card>
            )}
        </>
    );
};

export default MembersTab;
