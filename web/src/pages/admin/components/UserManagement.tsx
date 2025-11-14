import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
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
import { Search, MoreVertical, UserX, Trash2, Users } from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
    getAdminUsers,
    activateUser,
    deactivateUser,
    deleteUser,
    changeUserRole,
    type AdminUser,
} from "@/services/admin.service";
import { useAuth } from "@/contexts/AuthContext";

const UserManagement = () => {
    const { user: currentUser } = useAuth();
    const queryClient = useQueryClient();
    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");
    const [roleFilter, setRoleFilter] = useState<string>("all");
    const [statusFilter, setStatusFilter] = useState<string>("all");
    const [selectedUser, setSelectedUser] = useState<AdminUser | null>(null);
    const [action, setAction] = useState<"activate" | "deactivate" | "delete" | null>(null);
    const [isDialogOpen, setIsDialogOpen] = useState(false);

    const { data, isLoading, error } = useQuery({
        queryKey: ["admin-users", page, search, roleFilter, statusFilter],
        queryFn: () =>
            getAdminUsers(
                page,
                20,
                search || undefined,
                roleFilter === "all" ? undefined : roleFilter,
                statusFilter === "active" ? true : statusFilter === "inactive" ? false : undefined
            ),
    });

    const activateMutation = useMutation({
        mutationFn: activateUser,
        onSuccess: () => {
            toast.success("User activated successfully");
            queryClient.invalidateQueries({ queryKey: ["admin-users"] });
            queryClient.invalidateQueries({ queryKey: ["admin-statistics"] });
            setIsDialogOpen(false);
        },
        onError: (error: Error) => {
            toast.error(error.message || "Failed to activate user");
        },
    });

    const deactivateMutation = useMutation({
        mutationFn: deactivateUser,
        onSuccess: () => {
            toast.success("User deactivated successfully");
            queryClient.invalidateQueries({ queryKey: ["admin-users"] });
            queryClient.invalidateQueries({ queryKey: ["admin-statistics"] });
            setIsDialogOpen(false);
        },
        onError: (error: Error) => {
            toast.error(error.message || "Failed to deactivate user");
        },
    });

    const deleteMutation = useMutation({
        mutationFn: deleteUser,
        onSuccess: () => {
            toast.success("User deleted successfully");
            queryClient.invalidateQueries({ queryKey: ["admin-users"] });
            queryClient.invalidateQueries({ queryKey: ["admin-statistics"] });
            setIsDialogOpen(false);
        },
        onError: (error: Error) => {
            toast.error(error.message || "Failed to delete user");
        },
    });

    const handleAction = (user: AdminUser, actionType: "activate" | "deactivate" | "delete") => {
        setSelectedUser(user);
        setAction(actionType);
        setIsDialogOpen(true);
    };

    const confirmAction = () => {
        if (!selectedUser) return;

        switch (action) {
            case "activate":
                activateMutation.mutate(selectedUser.id);
                break;
            case "deactivate":
                deactivateMutation.mutate(selectedUser.id);
                break;
            case "delete":
                deleteMutation.mutate(selectedUser.id);
                break;
        }
    };

    const users = data?.content || [];
    const totalPages = data?.totalPages || 0;

    return (
        <div className="space-y-4">
            {/* Filters */}
            <div className="flex flex-col sm:flex-row gap-4">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                    <Input
                        placeholder="Search users by email, username, or name..."
                        value={search}
                        onChange={(e) => {
                            setSearch(e.target.value);
                            setPage(0);
                        }}
                        className="pl-10"
                    />
                </div>
                <Select value={roleFilter} onValueChange={(value) => {
                    setRoleFilter(value);
                    setPage(0);
                }}>
                    <SelectTrigger className="w-[180px]">
                        <SelectValue placeholder="All Roles" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="all">All Roles</SelectItem>
                        <SelectItem value="ADMIN">Admin</SelectItem>
                        <SelectItem value="PI">Principal Investigator</SelectItem>
                        <SelectItem value="RESEARCHER">Researcher</SelectItem>
                        <SelectItem value="STUDENT">Student</SelectItem>
                    </SelectContent>
                </Select>
                <Select value={statusFilter} onValueChange={(value) => {
                    setStatusFilter(value);
                    setPage(0);
                }}>
                    <SelectTrigger className="w-[180px]">
                        <SelectValue placeholder="All Status" />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectItem value="all">All Status</SelectItem>
                        <SelectItem value="active">Active</SelectItem>
                        <SelectItem value="inactive">Inactive</SelectItem>
                    </SelectContent>
                </Select>
            </div>

            {/* Users Table */}
            {isLoading ? (
                <Card>
                    <CardContent className="flex justify-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                    </CardContent>
                </Card>
            ) : error ? (
                <Card className="border-destructive">
                    <CardContent className="text-center py-12">
                        <p className="text-destructive font-medium">Error loading users</p>
                        <p className="text-sm text-muted-foreground mt-2">Please try again or check your connection</p>
                    </CardContent>
                </Card>
            ) : users.length === 0 ? (
                <Card>
                    <CardContent className="text-center py-12">
                        <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground" />
                        <h3 className="text-lg font-semibold mb-2">No users found</h3>
                        <p className="text-muted-foreground">
                            {search || (roleFilter !== "all") || (statusFilter !== "all")
                                ? "Try adjusting your filters"
                                : "No users in the system"}
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
                                    <TableHead>User</TableHead>
                                    <TableHead>Email</TableHead>
                                    <TableHead>Role</TableHead>
                                    <TableHead>Status</TableHead>
                                    <TableHead>Created</TableHead>
                                    <TableHead className="text-right">Actions</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {users.map((user) => (
                                    <TableRow key={user.id}>
                                        <TableCell>
                                            <div className="flex items-center gap-3">
                                                <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                                                    {user.avatarUrl ? (
                                                        <img
                                                            src={user.avatarUrl}
                                                            alt={user.fullName}
                                                            className="h-10 w-10 rounded-full"
                                                        />
                                                    ) : (
                                                        <Users className="h-5 w-5 text-primary" />
                                                    )}
                                                </div>
                                                <div>
                                                    <div className="font-medium">{user.fullName}</div>
                                                    <div className="text-sm text-muted-foreground">
                                                        @{user.username}
                                                    </div>
                                                </div>
                                            </div>
                                        </TableCell>
                                        <TableCell>{user.email}</TableCell>
                                        <TableCell>
                                            <Badge variant="outline">{user.role}</Badge>
                                        </TableCell>
                                        <TableCell>
                                            <Badge variant="secondary">Active</Badge>
                                        </TableCell>
                                        <TableCell>
                                            {new Date(user.createdDate).toLocaleDateString()}
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
                                                        onClick={() => handleAction(user, "deactivate")}
                                                        disabled={user.id === currentUser?.id}
                                                    >
                                                        <UserX className="h-4 w-4 mr-2" />
                                                        Deactivate
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem
                                                        onClick={() => handleAction(user, "delete")}
                                                        disabled={user.id === currentUser?.id}
                                                        className="text-destructive"
                                                    >
                                                        <Trash2 className="h-4 w-4 mr-2" />
                                                        Delete
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

            {/* Confirmation Dialog */}
            <AlertDialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>
                            {action === "activate" && "Activate User"}
                            {action === "deactivate" && "Deactivate User"}
                            {action === "delete" && "Delete User"}
                        </AlertDialogTitle>
                        <AlertDialogDescription>
                            {action === "activate" &&
                                `Are you sure you want to activate ${selectedUser?.fullName}?`}
                            {action === "deactivate" &&
                                `Are you sure you want to deactivate ${selectedUser?.fullName}? This will prevent them from accessing the system.`}
                            {action === "delete" &&
                                `Are you sure you want to delete ${selectedUser?.fullName}? This action cannot be undone.`}
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>Cancel</AlertDialogCancel>
                        <AlertDialogAction
                            onClick={confirmAction}
                            className={
                                action === "delete" ? "bg-destructive text-destructive-foreground" : ""
                            }
                        >
                            Confirm
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </div>
    );
};

export default UserManagement;

