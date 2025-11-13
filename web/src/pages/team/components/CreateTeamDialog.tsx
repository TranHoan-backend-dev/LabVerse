import React from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from '@/components/ui/dialog';
import {Input} from '@/components/ui/input';
import {Label} from '@/components/ui/label';
import {Textarea} from '@/components/ui/textarea';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select';
import {Button} from '@/components/ui/button';
import {Plus, Globe, Lock} from 'lucide-react';

type Props = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    teamName: string;
    description: string;
    researchField: string;
    privacy: 'PUBLIC' | 'PRIVATE';
    onNameChange: (name: string) => void;
    onDescriptionChange: (description: string) => void;
    onResearchFieldChange: (field: string) => void;
    onPrivacyChange: (privacy: 'PUBLIC' | 'PRIVATE') => void;
    onSubmit: () => void;
    isLoading: boolean;
};

const CreateTeamDialog: React.FC<Props> = ({
    open,
    onOpenChange,
    teamName,
    description,
    researchField,
    privacy,
    onNameChange,
    onDescriptionChange,
    onResearchFieldChange,
    onPrivacyChange,
    onSubmit,
    isLoading,
}) => {
    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogTrigger asChild>
                <Button size="lg" className="sm:w-auto w-full">
                    <Plus className="h-5 w-5 mr-2"/>
                    Create Team
                </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                <DialogHeader>
                    <DialogTitle>Create New Team</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                    <div className="space-y-2">
                        <Label htmlFor="team-name">Team Name *</Label>
                        <Input
                            id="team-name"
                            value={teamName}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => onNameChange(e.target.value)}
                            placeholder="e.g., Machine Learning Research Group"
                        />
                    </div>
                    <div className="space-y-2">
                        <Label htmlFor="description">Description</Label>
                        <Textarea
                            id="description"
                            value={description}
                            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => onDescriptionChange(e.target.value)}
                            placeholder="What is this team about?"
                            rows={4}
                        />
                    </div>
                    <div className="space-y-2">
                        <Label htmlFor="research-field">Research Field</Label>
                        <Input
                            id="research-field"
                            value={researchField}
                            onChange={(e: React.ChangeEvent<HTMLInputElement>) => onResearchFieldChange(e.target.value)}
                            placeholder="e.g., Artificial Intelligence, Bioinformatics"
                        />
                    </div>
                    <div className="space-y-2">
                        <Label htmlFor="privacy">Privacy *</Label>
                        <Select
                            value={privacy}
                            onValueChange={(value: 'PUBLIC' | 'PRIVATE') => onPrivacyChange(value)}
                        >
                            <SelectTrigger>
                                <SelectValue placeholder="Select privacy"/>
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="PUBLIC">
                                    <div className="flex items-center gap-2">
                                        <Globe className="h-4 w-4"/>
                                        Public - Anyone can find and join
                                    </div>
                                </SelectItem>
                                <SelectItem value="PRIVATE">
                                    <div className="flex items-center gap-2">
                                        <Lock className="h-4 w-4"/>
                                        Private - Invite only
                                    </div>
                                </SelectItem>
                            </SelectContent>
                        </Select>
                    </div>
                    <Button
                        onClick={onSubmit}
                        disabled={!teamName || isLoading}
                        className="w-full"
                    >
                        {isLoading ? 'Creating...' : 'Create Team'}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default CreateTeamDialog;
