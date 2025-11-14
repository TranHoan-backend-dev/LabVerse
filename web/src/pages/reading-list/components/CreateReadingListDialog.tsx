import React from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from '@/components/ui/dialog';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Textarea} from '@/components/ui/textarea';
import {Button} from '@/components/ui/button';
import {Plus} from 'lucide-react';

type Props = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    listName: string;
    description: string;
    onNameChange: (name: string) => void;
    onDescriptionChange: (description: string) => void;
    onSubmit: () => void;
    isLoading: boolean;
};

const CreateReadingListDialog: React.FC<Props> = ({
    open,
    onOpenChange,
    listName,
    description,
    onNameChange,
    onDescriptionChange,
    onSubmit,
    isLoading,
}) => {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogTrigger asChild>
                <Button size="lg" className="sm:w-auto w-full">
                    <Plus className="h-5 w-5 mr-2"/>
                    Create List
                </Button>
            </DialogTrigger>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create Reading List</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <div className="space-y-2">
                        <Label htmlFor="name">List Name</Label>
                        <Input
                            id="name"
                            value={listName}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => onNameChange(e.target.value)}
                            placeholder="e.g., Papers to Review This Week"
                        />
                    </div>
                    <div className="space-y-2">
                        <Label htmlFor="description">Description</Label>
                        <Textarea
                            id="description"
                            value={description}
                            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => onDescriptionChange(e.target.value)}
                            placeholder="What's this list for?"
                            rows={3}
                        />
                    </div>
                    <Button
                        onClick={onSubmit}
                        disabled={!listName || isLoading}
                        className="w-full"
                    >
                        {isLoading ? 'Creating...' : 'Create List'}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default CreateReadingListDialog;
