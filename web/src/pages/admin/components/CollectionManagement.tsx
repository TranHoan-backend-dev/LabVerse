import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { toast } from "sonner";
import { Search, MoreVertical, Trash2, FolderOpen } from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { getAdminCollections, deleteCollection, type AdminCollection } from "@/services/admin.service";
import { useAuth } from "@/contexts/AuthContext";

const CollectionManagement = () => {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");
    const [selectedCollection, setSelectedCollection] = useState<AdminCollection | null>(null);
    const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

    const { data, isLoading, error } = useQuery({
        queryKey: ["admin-collections", page, search],
        queryFn: () =>
            getAdminCollections(
                page,
                20,
                search || undefined
            ),
    });

    const deleteMutation = useMutation({
        mutationFn: (collectionId: string) => {
            if (!user?.id) {
                throw new Error("User not authenticated");
            }
            return deleteCollection(collectionId, user.id);
        },
        onSuccess: () => {
            toast.success("Collection deleted successfully");
            queryClient.invalidateQueries({ queryKey: ["admin-collections"] });
            queryClient.invalidateQueries({ queryKey: ["admin-statistics"] });
            setIsDeleteDialogOpen(false);
        },
        onError: (error: Error) => {
            toast.error(error.message || "Failed to delete collection");
        },
    });

    const handleDelete = (collection: AdminCollection) => {
        setSelectedCollection(collection);
        setIsDeleteDialogOpen(true);
    };

    const confirmDelete = () => {
        if (selectedCollection) {
            deleteMutation.mutate(selectedCollection.id);
        }
    };

    const collections = data?.content || [];
    const totalPages = data?.totalPages || 0;

    return (
        <div className="space-y-4">
            {/* Filters */}
            <div className="flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                        placeholder="Search collections by name..."
                        value={search}
                        onChange={(e) => {
                            setSearch(e.target.value);
                            setPage(0);
                        }}
                        className="pl-10"
                    />
                </div>
            </div>

            {/* Collections Table */}
            {isLoading ? (
                <Card>
                    <CardContent className="flex justify-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                    </CardContent>
                </Card>
            ) : error ? (
                <Card className="border-destructive">
                    <CardContent className="text-center py-12">
                        <p className="text-destructive font-medium">Error loading collections</p>
                        <p className="text-sm text-muted-foreground mt-2">Please try again or check your connection</p>
                    </CardContent>
                </Card>
            ) : collections.length === 0 ? (
                <Card>
                    <CardContent className="text-center py-12">
                        <FolderOpen className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                        <h3 className="text-lg font-semibold mb-2">No collections found</h3>
                        <p className="text-muted-foreground">
                            {search
                                ? "Try adjusting your filters"
                                : "No collections in the system"}
                        </p>
                    </CardContent>
                </Card>
            ) : (
                <>
                    <Card>
                        <div className="border rounded-lg overflow-hidden">
                            <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Collection Name</TableHead>
                                    <TableHead>Papers</TableHead>
                                    <TableHead>Members</TableHead>
                                    <TableHead>Created By</TableHead>
                                    <TableHead className="text-right">Actions</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {collections.map((collection) => (
                                    <TableRow key={collection.id}>
                                        <TableCell className="font-medium">{collection.name}</TableCell>
                                        <TableCell>
                                            <Badge variant="outline">{collection.paperCount || 0}</Badge>
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant="secondary">{collection.memberCount || 0}</Badge>
                                        </TableCell>
                                        <TableCell>
                                            <div>
                                                <div className="font-medium">{collection.creatorName || "Unknown"}</div>
                                                {collection.creatorEmail && (
                                                    <div className="text-sm text-muted-foreground">
                                                        {collection.creatorEmail}
                                                    </div>
                                                )}
                                            </div>
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button variant="ghost" size="sm">
                                                        <MoreVertical className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem
                                                        onClick={() => handleDelete(collection)}
                                                        className="text-destructive"
                                                    >
                                                        <Trash2 className="h-4 w-4 mr-2" />
                                                        Delete Collection
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                        </div>
                    </Card>

                    {/* Pagination */}
                    {totalPages > 1 && (
                        <div className="flex justify-center items-center gap-3">
                            <Button
                                variant="outline"
                                disabled={page === 0}
                                onClick={() => setPage(page - 1)}
                            >
                                Previous
                            </Button>
                            <span className="text-sm font-medium">
                                Page {page + 1} / {totalPages}
                            </span>
                            <Button
                                variant="outline"
                                disabled={page >= totalPages - 1}
                                onClick={() => setPage(page + 1)}
                            >
                                Next
                            </Button>
                        </div>
                    )}
                </>
            )}

            {/* Delete Confirmation Dialog */}
            <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>Delete Collection</AlertDialogTitle>
                        <AlertDialogDescription>
                            Are you sure you want to delete "{selectedCollection?.name}"? This action
                            cannot be undone. All papers and members will be removed from this collection.
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={confirmDelete}
                            className="bg-destructive text-destructive-foreground"
                        >
                            Delete
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
};

export default CollectionManagement;

