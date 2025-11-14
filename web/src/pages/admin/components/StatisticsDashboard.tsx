import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getAdminStatistics } from "@/services/admin.service";
import { Users, FileText, FolderOpen, TrendingUp, Activity } from "lucide-react";

const StatisticsDashboard = () => {
    const { data: stats, isLoading } = useQuery({
        queryKey: ["admin-statistics"],
        queryFn: getAdminStatistics,
    });

    if (isLoading) {
        return (
            <div className="flex justify-center py-12">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
        );
    }

    if (!stats) {
        return (
            <div className="text-center py-12">
                <p className="text-destructive">Failed to load statistics</p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* User Statistics */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Users className="h-5 w-5" />
                        User Statistics
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <div>
                            <div className="text-2xl font-bold">{stats.totalUsers}</div>
                            <div className="text-sm text-muted-foreground">Total Users</div>
                        </div>
                        <div>
                            <div className="text-2xl font-bold text-green-600">
                                {stats.activeUsers}
                            </div>
                            <div className="text-sm text-muted-foreground">Active Users</div>
                        </div>
                        <div>
                            <div className="text-2xl font-bold text-red-600">
                                {stats.inactiveUsers}
                            </div>
                            <div className="text-sm text-muted-foreground">Inactive Users</div>
                        </div>
                        <div>
                            <div className="text-2xl font-bold">
                                {stats.totalUsers > 0
                                    ? Math.round((stats.activeUsers / stats.totalUsers) * 100)
                                    : 0}
                                %
                            </div>
                            <div className="text-sm text-muted-foreground">Active Rate</div>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* Paper Statistics */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <FileText className="h-5 w-5" />
                        Paper Statistics
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                        <div>
                            <div className="text-2xl font-bold">{stats.totalPapers}</div>
                            <div className="text-sm text-muted-foreground">Total Papers</div>
                        </div>
                        <div>
                            <div className="text-2xl font-bold text-blue-600">
                                {stats.papersThisMonth}
                            </div>
                            <div className="text-sm text-muted-foreground">This Month</div>
                        </div>
                        <div>
                            <div className="text-2xl font-bold">
                                {stats.totalUsers > 0
                                    ? Math.round(stats.totalPapers / stats.totalUsers)
                                    : 0}
                            </div>
                            <div className="text-sm text-muted-foreground">Avg per User</div>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* Collection Statistics */}
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <FolderOpen className="h-5 w-5" />
                        Collection Statistics
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                            <div className="text-3xl font-bold">{stats.totalCollections}</div>
                            <div className="text-sm text-muted-foreground">Total Collections</div>
                        </div>
                        <div>
                            <div className="text-3xl font-bold text-purple-600">
                                {stats.totalUsers > 0
                                    ? Math.round(stats.totalCollections / stats.totalUsers * 10) / 10
                                    : 0}
                            </div>
                            <div className="text-sm text-muted-foreground">Avg per User</div>
                        </div>
                        <div>
                            <div className="text-3xl font-bold text-blue-600">
                                {stats.totalPapers > 0 && stats.totalCollections > 0
                                    ? Math.round(stats.totalPapers / stats.totalCollections)
                                    : 0}
                            </div>
                            <div className="text-sm text-muted-foreground">Avg Papers per Collection</div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
};

export default StatisticsDashboard;

