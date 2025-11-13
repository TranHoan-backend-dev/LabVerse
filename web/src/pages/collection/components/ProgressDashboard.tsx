import {useQuery} from "@tanstack/react-query";
import {Card, CardContent, CardHeader, CardTitle, CardDescription} from "@/components/ui/card";
import {Progress} from "@/components/ui/progress";
import {Badge} from "@/components/ui/badge";
import {Loader2, Users, FileText, BookOpen, CheckCircle2, Clock} from "lucide-react";
import {getCollectionProgress, type CollectionProgressStatisticsResponse} from "@/services/progress.service";
import {getCollectionMembers, type CollectionUserResponse} from "@/services/collection.service";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";

interface ProgressDashboardProps {
    collectionId: string;
    members?: CollectionUserResponse[];
}

const ProgressDashboard = ({collectionId, members}: ProgressDashboardProps) => {
    const {data: progressData, isLoading, error} = useQuery({
        queryKey: ['collection-progress', collectionId],
        queryFn: () => getCollectionProgress(collectionId),
        refetchInterval: 30000, // Refetch every 30 seconds
    });

    if (isLoading) {
        return (
            <div className="flex justify-center items-center py-12">
                <Loader2 className="h-8 w-8 animate-spin text-muted-foreground"/>
            </div>
        );
    }

    if (error) {
        return (
            <Card>
                <CardContent className="text-center py-12">
                    <p className="text-muted-foreground">Failed to load progress data</p>
                </CardContent>
            </Card>
        );
    }

    if (!progressData) {
        return (
            <Card>
                <CardContent className="text-center py-12">
                    <p className="text-muted-foreground">No progress data available</p>
                </CardContent>
            </Card>
        );
    }

    // Create a map of userId to member info for easy lookup
    const memberMap = new Map<string, CollectionUserResponse>();
    if (members) {
        members.forEach(member => {
            memberMap.set(member.memberId, member);
        });
    }

    const getStatusColor = (status: string) => {
        switch (status.toLowerCase()) {
            case 'finished':
                return 'bg-green-500';
            case 'reading':
                return 'bg-blue-500';
            case 'unread':
            case 'toread':
                return 'bg-gray-500';
            default:
                return 'bg-gray-500';
        }
    };

    const getStatusIcon = (status: string) => {
        switch (status.toLowerCase()) {
            case 'finished':
                return <CheckCircle2 className="h-4 w-4"/>;
            case 'reading':
                return <BookOpen className="h-4 w-4"/>;
            case 'unread':
            case 'toread':
                return <Clock className="h-4 w-4"/>;
            default:
                return <Clock className="h-4 w-4"/>;
        }
    };

    return (
        <div className="space-y-6">
            {/* Overview Statistics */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Total Papers</CardTitle>
                        <FileText className="h-4 w-4 text-muted-foreground"/>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{progressData.totalPapers}</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Team Members</CardTitle>
                        <Users className="h-4 w-4 text-muted-foreground"/>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{progressData.totalUsers}</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Average Progress</CardTitle>
                        <BookOpen className="h-4 w-4 text-muted-foreground"/>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{progressData.averageProgress.toFixed(1)}%</div>
                        <Progress value={progressData.averageProgress} className="mt-2"/>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Finished</CardTitle>
                        <CheckCircle2 className="h-4 w-4 text-muted-foreground"/>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{progressData.finishedCount}</div>
                        <p className="text-xs text-muted-foreground mt-1">
                            of {progressData.totalPapers} papers
                        </p>
                    </CardContent>
                </Card>
            </div>

            {/* Status Distribution */}
            <Card>
                <CardHeader>
                    <CardTitle>Reading Status Distribution</CardTitle>
                    <CardDescription>Overview of reading status across all papers</CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="space-y-4">
                        {progressData.statusDistribution.map((status) => (
                            <div key={status.status} className="space-y-2">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-2">
                                        {getStatusIcon(status.status)}
                                        <span className="font-medium capitalize">{status.status}</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <span className="text-sm text-muted-foreground">
                                            {status.count} papers
                                        </span>
                                        <Badge variant="outline">{status.percentage.toFixed(1)}%</Badge>
                                    </div>
                                </div>
                                <Progress value={status.percentage} className="h-2"/>
                            </div>
                        ))}
                    </div>
                </CardContent>
            </Card>

            {/* Team Member Progress */}
            <Card>
                <CardHeader>
                    <CardTitle>Team Member Progress</CardTitle>
                    <CardDescription>Individual reading progress for each team member</CardDescription>
                </CardHeader>
                <CardContent>
                    {progressData.teamMemberProgress.length === 0 ? (
                        <p className="text-center text-muted-foreground py-8">
                            No team member progress data available
                        </p>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>Member</TableHead>
                                    <TableHead>Total Papers</TableHead>
                                    <TableHead>Unread</TableHead>
                                    <TableHead>Reading</TableHead>
                                    <TableHead>Finished</TableHead>
                                    <TableHead>Avg Progress</TableHead>
                                    <TableHead>Progress</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {progressData.teamMemberProgress.map((memberProgress) => {
                                    const member = memberMap.get(memberProgress.userId);
                                    return (
                                        <TableRow key={memberProgress.userId}>
                                            <TableCell className="font-medium">
                                                {member ? member.memberName : `User ${memberProgress.userId.substring(0, 8)}`}
                                            </TableCell>
                                            <TableCell>{memberProgress.totalPapers}</TableCell>
                                            <TableCell>
                                                <Badge variant="outline" className="bg-gray-100">
                                                    {memberProgress.unreadCount}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>
                                                <Badge variant="outline" className="bg-blue-100">
                                                    {memberProgress.readingCount}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>
                                                <Badge variant="outline" className="bg-green-100">
                                                    {memberProgress.finishedCount}
                                                </Badge>
                                            </TableCell>
                                            <TableCell>
                                                {memberProgress.averageProgress.toFixed(1)}%
                                            </TableCell>
                                            <TableCell>
                                                <Progress 
                                                    value={memberProgress.averageProgress} 
                                                    className="w-24"
                                                />
                                            </TableCell>
                                        </TableRow>
                                    );
                                })}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>
        </div>
    );
};

export default ProgressDashboard;

