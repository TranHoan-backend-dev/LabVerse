import React from 'react';
import {Button} from '@/components/ui/button';
import {ArrowLeft, Plus, Users} from 'lucide-react';
import type { CollectionResponse } from '@/services/collection.service';

type Props = {
    collection: CollectionResponse;
    navigate: (path: string) => void;
    activeTab: 'papers' | 'members' | 'progress';
    setActiveTab: (v: 'papers' | 'members' | 'progress') => void;
    isOwner: boolean;
    canAddPaper: boolean;
    canManageMembers: boolean;
    openAddPaper: () => void;
    openAddMember: () => void;
};

const CollectionHeader: React.FC<Props> = ({collection, navigate, activeTab, setActiveTab, isOwner, canAddPaper, canManageMembers, openAddPaper, openAddMember}) => {
    return (
        <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => navigate('/collections')}>
                <ArrowLeft className="h-5 w-5"/>
            </Button>
            <div className="flex-1">
                <h1 className="text-3xl font-bold">{collection.name}</h1>
                <div className="flex items-center gap-4 mt-2 text-sm text-muted-foreground">
                    <span>Papers: {collection.paperCount || 0}</span>
                    <span>Members: {collection.memberCount || 0}</span>
                </div>
            </div>
            <div className="flex gap-2">
                {canAddPaper && (
                    <Button onClick={openAddPaper}>
                        <Plus className="h-4 w-4 mr-2"/>
                        Add Paper
                    </Button>
                )}
                {canManageMembers && (
                    <Button onClick={() => { /* handled by parent via set state if needed */ }}>
                        <Users className="h-4 w-4 mr-2"/>
                        Members
                    </Button>
                )}
            </div>
        </div>
    );
};

export default CollectionHeader;
