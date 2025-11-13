import React, { Dispatch } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Users } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import type { CollectionUserResponse } from '@/services/collection.service';
import type { UseMutationResult } from '@tanstack/react-query';

type Props = {
    isLoadingMembers: boolean;
    members: CollectionUserResponse[] | undefined;
    canManageMembers: boolean;
    isAddMemberOpen: boolean;
    setIsAddMemberOpen: (b: boolean) => void;
    newMemberEmail: string;
    setNewMemberEmail: (s: string) => void;
    newMemberAccessLevel: 'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR';
    setNewMemberAccessLevel: Dispatch<React.SetStateAction<'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR'>>;
    addMemberMutation: UseMutationResult<unknown, unknown, unknown, unknown>;
    handleRemoveMember: (m: CollectionUserResponse) => void;
};

const MembersTab: React.FC<Props> = ({
    isLoadingMembers,
    members,
    isAddMemberOpen,
    setIsAddMemberOpen,
    newMemberEmail,
    setNewMemberEmail,
    newMemberAccessLevel,
    setNewMemberAccessLevel,
    addMemberMutation,
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
                                    <div className="flex-1">
                                        <CardTitle className="text-lg font-semibold mb-1">{member.memberName}</CardTitle>
                                        <p className="text-xs text-muted-foreground mb-3">{member.memberEmail}</p>
                                        <div className="flex gap-2">
                                            <span className="badge">{member.accessLevel}</span>
                                            <span className="badge">{member.role}</span>
                                        </div>
                                    </div>
                                    <div>
                                        <Button variant="ghost" size="icon" onClick={() => handleRemoveMember(member)}>
                                            <Users className="h-4 w-4" />
                                        </Button>
                                    </div>
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
                        <p className="text-muted-foreground">Add members to this collection to start collaborating</p>
                    </CardContent>
                </Card>
            )}

            {/* Invite member dialog */}
            <Dialog open={isAddMemberOpen} onOpenChange={(open) => setIsAddMemberOpen(open)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Invite Member to Collection</DialogTitle>
                    </DialogHeader>
                    <div className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="member-email">Member Email</Label>
                            <Input
                                id="member-email"
                                type="email"
                                value={newMemberEmail}
                                onChange={(e: React.ChangeEvent<HTMLInputElement>) => setNewMemberEmail(e.target.value)}
                                placeholder="Enter member email address"
                            />
                            <p className="text-xs text-muted-foreground">Enter the email address of the user you want to invite.</p>
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="access-level">Access Level</Label>
                            <Select value={newMemberAccessLevel} onValueChange={(v) => setNewMemberAccessLevel(v as 'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR')}>
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="READ_ONLY">Read Only</SelectItem>
                                    <SelectItem value="CONTRIBUTOR">Contributor</SelectItem>
                                    <SelectItem value="AUTHOR">Author</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                        <div className="flex gap-2">
                            <Button variant="outline" onClick={() => setIsAddMemberOpen(false)} className="flex-1">Cancel</Button>
                            <Button
                                onClick={() => addMemberMutation.mutate({ email: newMemberEmail, accessLevel: newMemberAccessLevel })}
                                className="flex-1"
                            >
                                Invite
                            </Button>
                        </div>
                    </div>
                </DialogContent>
            </Dialog>
        </>
    );
};

export default MembersTab;
