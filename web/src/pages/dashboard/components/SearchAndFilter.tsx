import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Search, Filter } from "lucide-react";
import { useState } from "react";

interface SearchAndFilterProps {
    searchQuery: string;
    setSearchQuery: (query: string) => void;
    filters: {
        author: string,
        journal: string,
        yearFrom: string,
        yearTo: string,
    }
    setFilters: React.Dispatch<React.SetStateAction<{
        author: string;
        journal: string;
        yearFrom: string,
        yearTo: string,
    }>>;
}

const SearchAndFilter = ({
    searchQuery,
    setSearchQuery,
    filters,
    setFilters,
}: SearchAndFilterProps) => {
    const resetFilter = () => {
        setFilters({
            author: '',
            journal: '',
            yearFrom: '',
            yearTo: '',
        });
    }

    return (
        <div className="rounded-xl border border-border bg-card p-6 shadow-custom-sm">
            <div className="flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search
                        className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
                    <Input
                        placeholder="Search papers by title or author..."
                        className="pl-10"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
                <Dialog>
                    <DialogTrigger asChild>
                        <Button variant="outline">
                            <Filter className="h-5 w-5 mr-2" />
                            Filters
                        </Button>
                    </DialogTrigger>

                    <DialogContent className="max-w-md">
                        <DialogHeader>
                            <DialogTitle>Filters</DialogTitle>
                        </DialogHeader>

                        <div className="space-y-4">
                            <div className="space-y-2">
                                <Label>Author</Label>
                                <Input
                                    value={filters.author}
                                    onChange={(e) => setFilters({ ...filters, author: e.target.value })}
                                    placeholder="John Doe"
                                />
                            </div>

                            <div className="space-y-2">
                                <Label>Journal</Label>
                                <Input
                                    value={filters.journal}
                                    onChange={(e) => setFilters({ ...filters, journal: e.target.value })}
                                    placeholder="Nature, Science..."
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <Label>Year From</Label>
                                    <Input
                                        type="number"
                                        value={filters.yearFrom}
                                        onChange={(e) => setFilters({
                                            ...filters,
                                            yearFrom: e.target.value
                                        })}
                                        placeholder="1990"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label>Year To</Label>
                                    <Input
                                        type="number"
                                        value={filters.yearTo}
                                        onChange={(e) => setFilters({
                                            ...filters,
                                            yearTo: e.target.value
                                        })}
                                        placeholder="2024"
                                    />
                                </div>
                            </div>

                            <div className="space-y-2 text-center">
                                <Button variant="outline" onClick={resetFilter}>
                                    Reset
                                </Button>
                            </div>
                        </div>
                    </DialogContent>
                </Dialog>
            </div>
        </div>
    );
};

export default SearchAndFilter;