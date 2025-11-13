import React from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle} from '@/components/ui/dialog';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Button} from '@/components/ui/button';

type Props = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    collectionName: string;
    onNameChange: (name: string) => void;
    onSubmit: () => void;
    isLoading: boolean;
};

const EditCollectionDialog: React.FC<Props> = ({
    open,
    onOpenChange,
    collectionName,
    onNameChange,
    onSubmit,
    isLoading,
}) => {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Edit Collection</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <div className="space-y-2">
                        <Label htmlFor="edit-name">Collection Name</Label>
                        <Input
                            id="edit-name"
                            value={collectionName}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => onNameChange(e.target.value)}
                            placeholder="Collection name"
                        />
                    </div>
                    <Button
                        onClick={onSubmit}
                        disabled={!collectionName || isLoading}
                        className="w-full"
                    >
                        {isLoading ? 'Saving...' : 'Save Changes'}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default EditCollectionDialog;
