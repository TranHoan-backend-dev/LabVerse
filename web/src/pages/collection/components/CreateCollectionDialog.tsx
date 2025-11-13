import React from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from '@/components/ui/dialog';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Button} from '@/components/ui/button';
import {Plus} from 'lucide-react';

type Props = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    collectionName: string;
    onNameChange: (name: string) => void;
    onSubmit: () => void;
    isLoading: boolean;
};

const CreateCollectionDialog: React.FC<Props> = ({
    open,
    onOpenChange,
    collectionName,
    onNameChange,
    onSubmit,
    isLoading,
}) => {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogTrigger asChild>
                <Button size="lg" className="sm:w-auto w-full">
                    <Plus className="h-5 w-5 mr-2"/>
                    Create Collection
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create New Collection</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <div className="space-y-2">
                        <Label htmlFor="name">Collection Name</Label>
                        <Input
                            id="name"
                            value={collectionName}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => onNameChange(e.target.value)}
                            placeholder="e.g., Machine Learning Papers"
                        />
                    </div>
                    <Button
                        onClick={onSubmit}
                        disabled={!collectionName || isLoading}
                        className="w-full"
                    >
                        {isLoading ? 'Creating...' : 'Create Collection'}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default CreateCollectionDialog;
