import React from 'react';
import {Input} from '@/components/ui/input';
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from '@/components/ui/select';
import {Search} from 'lucide-react';

type Props = {
    searchQuery: string;
    privacyFilter: 'PUBLIC' | 'PRIVATE' | '';
    onSearchChange: (query: string) => void;
    onPrivacyFilterChange: (filter: 'PUBLIC' | 'PRIVATE' | '') => void;
};

const TeamsSearchFilter: React.FC<Props> = ({
    searchQuery,
    privacyFilter,
    onSearchChange,
    onPrivacyFilterChange,
}) => {
    return (
        <div className="flex flex-col sm:flex-row gap-4">
            <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground"/>
                <Input
                    placeholder="Search teams..."
                    value={searchQuery}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => onSearchChange(e.target.value)}
                    className="pl-10"
                />
            </div>
            <Select
                value={privacyFilter === '' ? 'ALL' : privacyFilter}
                onValueChange={(value: string) => onPrivacyFilterChange(value === 'ALL' ? '' : (value as 'PUBLIC' | 'PRIVATE'))}
            >
                <SelectTrigger className="w-full sm:w-[180px]">
                    <SelectValue placeholder="All teams"/>
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value="ALL">All teams</SelectItem>
                    <SelectItem value="PUBLIC">Public</SelectItem>
                    <SelectItem value="PRIVATE">Private</SelectItem>
                </SelectContent>
            </Select>
        </div>
    );
};

export default TeamsSearchFilter;
