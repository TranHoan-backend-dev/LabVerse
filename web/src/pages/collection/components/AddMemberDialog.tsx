import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Plus, Users, Loader2 } from 'lucide-react';
import type { UseMutationResult } from '@tanstack/react-query';

type Props = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    newMemberEmail: string;
    setNewMemberEmail: (s: string) => void;
    newMemberAccessLevel: 'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR';
    setNewMemberAccessLevel: React.Dispatch<React.SetStateAction<'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR'>>;
    addMemberMutation: UseMutationResult<unknown, unknown, unknown, unknown>;
};

const AddMemberDialog: React.FC<Props> = ({
    open,
    onOpenChange,
    newMemberEmail,
    setNewMemberEmail,
    newMemberAccessLevel,
    setNewMemberAccessLevel,
    addMemberMutation,
}) => {
    return (
        <Dialog
            open={open}
            onOpenChange={(open) => {
                onOpenChange(open);
                if (!open) {
                    setNewMemberEmail('');
                    setNewMemberAccessLevel('CONTRIBUTOR');
                }
            }}
        >
            <DialogTrigger asChild>
                <Button>
                    <Plus className="h-4 w-4 mr-2" />
                    Invite Member
                </Button>
            </DialogTrigger>
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
                            onChange={(e) => setNewMemberEmail(e.target.value)}
                            placeholder="Enter member email address"
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' && newMemberEmail && !addMemberMutation.isPending) {
                                    addMemberMutation.mutate();
                                }
                            }}
                        />
                        <p className="text-xs text-muted-foreground">
                            Enter the email address of the user you want to invite. They must have an account on LabVerse.
                        </p>
                    </div>
                    <div className="space-y-2">
                        <Label htmlFor="access-level">Access Level</Label>
                        <Select
                            value={newMemberAccessLevel}
                            onValueChange={(v) => setNewMemberAccessLevel(v as typeof newMemberAccessLevel)}
                        >
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="READ_ONLY">Read Only - Can view papers only</SelectItem>
                                <SelectItem value="CONTRIBUTOR">Contributor - Can add papers and update status</SelectItem>
                                <SelectItem value="AUTHOR">Author - Full access including priority and members</SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <div className="flex gap-2">
                        <Button
                            variant="outline"
                            onClick={() => onOpenChange(false)}
                            className="flex-1"
                        >
                            Cancel
                        </Button>
                        <Button
                            onClick={() => addMemberMutation.mutate()}
                            disabled={!newMemberEmail || addMemberMutation.isPending || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(newMemberEmail)}
                            className="flex-1"
                        >
                            {addMemberMutation.isPending ? (
                                <>
                                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                    Inviting...
                                </>
                            ) : (
                                <>
                                    <Users className="h-4 w-4 mr-2" />
                                    Send Invite
                                </>
                            )}
                        </Button>
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default AddMemberDialog;

