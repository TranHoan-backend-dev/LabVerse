import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Users, FileText, FolderOpen, BarChart3, Shield, TrendingUp, Activity } from "lucide-react";
import Header from "@/pages/Header";
import AdminRoute from "@/components/AdminRoute";
import UserManagement from "./components/UserManagement";
import CollectionManagement from "./components/CollectionManagement";
import StatisticsDashboard from "./components/StatisticsDashboard";
import { getAdminStatistics } from "@/services/admin.service";
import { Helmet } from "react-helmet-async";

const AdminDashboard = () => {
    const { data: stats, isLoading: statsLoading, error: statsError } = useQuery({
        queryKey: ["admin-statistics"],
        queryFn: getAdminStatistics,
        retry: 1,
    });

    return (
        <AdminRoute>
            <Helmet>
                <title>Admin Dashboard – LabVerse</title>
                <meta name="description" content="Admin dashboard for managing users, papers, teams, and viewing system statistics" />
            </Helmet>
            <div className="min-h-screen bg-gradient-to-br from-background via-background to-muted/20">
                <Header />
                <main className="container mx-auto px-4 py-8 max-w-7xl">
                    {/* Header Section */}
                    <div className="mb-8">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="h-12 w-12 rounded-lg bg-primary/10 flex items-center justify-center">
                                <Shield className="h-6 w-6 text-primary" />
                            </div>
                            <div>
                                <h1 className="text-3xl font-bold">Admin Dashboard</h1>
                                <p className="text-muted-foreground">Manage users, papers, collections, and view system statistics</p>
                            </div>
                        </div>
                    </div>

                    {/* Statistics Cards */}
                    {statsLoading ? (
                        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-8">
                            {[1, 2, 3, 4].map((i) => (
                                <Card key={i} className="border-2">
                                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                        <CardTitle className="text-sm font-medium">Loading...</CardTitle>
                                        <div className="h-4 w-4 bg-muted animate-pulse rounded"></div>
                                    </CardHeader>
                                    <CardContent>
                                        <div className="h-8 w-16 bg-muted animate-pulse rounded mb-2"></div>
                                        <div className="h-4 w-24 bg-muted animate-pulse rounded"></div>
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    ) : statsError ? (
                        <Card className="mb-8 border-destructive">
                            <CardHeader>
                                <CardTitle className="text-destructive">Error Loading Statistics</CardTitle>
                                <CardDescription>
                                    Failed to load statistics. Please refresh the page or check your connection.
                                </CardDescription>
                            </CardHeader>
                        </Card>
                    ) : (
                        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4 mb-8">
                            <Card className="border-2 hover:shadow-lg transition-shadow">
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium text-muted-foreground">Total Users</CardTitle>
                                    <div className="h-10 w-10 rounded-lg bg-blue-100 dark:bg-blue-900/20 flex items-center justify-center">
                                        <Users className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                                    </div>
                                </CardHeader>
                                <CardContent>
                                    <div className="text-3xl font-bold mb-1">{stats?.totalUsers || 0}</div>
                                    <p className="text-xs text-muted-foreground flex items-center gap-1">
                                        <Activity className="h-3 w-3" />
                                        {stats?.activeUsers || 0} active
                                    </p>
                                </CardContent>
                            </Card>

                            <Card className="border-2 hover:shadow-lg transition-shadow">
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium text-muted-foreground">Total Papers</CardTitle>
                                    <div className="h-10 w-10 rounded-lg bg-green-100 dark:bg-green-900/20 flex items-center justify-center">
                                        <FileText className="h-5 w-5 text-green-600 dark:text-green-400" />
                                    </div>
                                </CardHeader>
                                <CardContent>
                                    <div className="text-3xl font-bold mb-1">{stats?.totalPapers || 0}</div>
                                    <p className="text-xs text-muted-foreground flex items-center gap-1">
                                        <TrendingUp className="h-3 w-3" />
                                        {stats?.papersThisMonth || 0} this month
                                    </p>
                                </CardContent>
                            </Card>

                            <Card className="border-2 hover:shadow-lg transition-shadow">
                                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                                    <CardTitle className="text-sm font-medium text-muted-foreground">Collections</CardTitle>
                                    <div className="h-10 w-10 rounded-lg bg-purple-100 dark:bg-purple-900/20 flex items-center justify-center">
                                        <FolderOpen className="h-5 w-5 text-purple-600 dark:text-purple-400" />
                                    </div>
                                </CardHeader>
                                <CardContent>
                                    <div className="text-3xl font-bold mb-1">{stats?.totalCollections || 0}</div>
                                    <p className="text-xs text-muted-foreground">
                                        Total collections
                                    </p>
                                </CardContent>
                            </Card>
                        </div>
                    )}

                    {/* Management Tabs */}
                    <Tabs defaultValue="users" className="space-y-4">
                        <TabsList className="grid w-full grid-cols-3 lg:w-auto">
                            <TabsTrigger value="users" className="flex items-center gap-2">
                                <Users className="h-4 w-4" />
                                Users
                            </TabsTrigger>
                            <TabsTrigger value="collections" className="flex items-center gap-2">
                                <FolderOpen className="h-4 w-4" />
                                Collections
                            </TabsTrigger>
                            <TabsTrigger value="statistics" className="flex items-center gap-2">
                                <BarChart3 className="h-4 w-4" />
                                Statistics
                            </TabsTrigger>
                        </TabsList>

                        <TabsContent value="users">
                            <UserManagement />
                        </TabsContent>

                        <TabsContent value="collections">
                            <CollectionManagement />
                        </TabsContent>

                        <TabsContent value="statistics">
                            <StatisticsDashboard />
                        </TabsContent>
                    </Tabs>
                </main>
            </div>
        </AdminRoute>
    );
};

export default AdminDashboard;

