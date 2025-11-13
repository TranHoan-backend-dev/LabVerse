import React from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import type { CollectionPaperDetailResponse } from '@/services/collection.service';
import type { UseMutationResult } from '@tanstack/react-query';

type Props = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    selectedPaper: CollectionPaperDetailResponse | null;
    selectedPriority: string;
    setSelectedPriority: (s: string) => void;
    updatePriorityMutation: UseMutationResult<unknown, unknown, unknown, unknown>;
};

const PriorityDialog: React.FC<Props> = ({ open, onOpenChange, selectedPaper, selectedPriority, setSelectedPriority, updatePriorityMutation }) => {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Update Paper Priority</DialogTitle>
                    <p className="text-sm text-muted-foreground mt-1">Status is calculated automatically based on all members' reading progress</p>
                </DialogHeader>
                {selectedPaper && (
                    <div className="space-y-4">
                        <div className="space-y-2">
                            <Label htmlFor="priority">Priority</Label>
                            <Select value={selectedPriority} onValueChange={setSelectedPriority}>
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="HIGH">High</SelectItem>
                                    <SelectItem value="MEDIUM">Medium</SelectItem>
                                    <SelectItem value="LOW">Low</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                        <Button
                            onClick={() => updatePriorityMutation.mutate(selectedPriority)}
                            className="w-full"
                            disabled={updatePriorityMutation?.status === 'pending'}
                        >
                            {updatePriorityMutation?.status === 'pending' ? 'Updating...' : 'Update Priority'}
                        </Button>
                    </div>
                )}
            </DialogContent>
        </Dialog>
    );
};

export default PriorityDialog;
