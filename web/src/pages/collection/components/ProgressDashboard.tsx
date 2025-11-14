import {useQuery} from "@tanstack/react-query";
import {Card, CardContent, CardHeader, CardTitle, CardDescription} from "@/components/ui/card";
import {Progress} from "@/components/ui/progress";
import {Badge} from "@/components/ui/badge";
import {Loader2, Users, FileText, BookOpen, CheckCircle2, Clock} from "lucide-react";
import {getCollectionProgress, type CollectionProgressStatisticsResponse} from "@/services/progress.service";
import {getCollectionMembers, getPapersInCollection, type CollectionUserResponse, type CollectionPaperDetailResponse} from "@/services/collection.service";
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
    // Fetch actual papers in collection
    const {data: papers, isLoading: isLoadingPapers} = useQuery({
        queryKey: ['collection-papers', collectionId],
        queryFn: () => getPapersInCollection(collectionId),
    });

    // Fetch progress data from workflows
    const {data: progressData, isLoading: isLoadingProgress, error} = useQuery({
        queryKey: ['collection-progress', collectionId],
        queryFn: () => getCollectionProgress(collectionId),
        refetchInterval: 30000, // Refetch every 30 seconds
    });

    const isLoading = isLoadingPapers || isLoadingProgress;

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

    if (!progressData || !papers) {
        return (
            <Card>
                <CardContent className="text-center py-12">
                    <p className="text-muted-foreground">No progress data available</p>
                </CardContent>
            </Card>
        );
    }

    // Recalculate statistics based on actual papers in collection
    const actualTotalPapers = papers.length;
    
    // Calculate status distribution based on papers (not workflows)
    // Status is calculated automatically by backend based on all members' progress
    const statusCounts = new Map<string, number>();
    
    papers.forEach(paper => {
        const status = paper.status || 'ToRead';
        // Normalize status to lowercase for consistency
        const normalizedStatus = status.toLowerCase();
        statusCounts.set(normalizedStatus, (statusCounts.get(normalizedStatus) || 0) + 1);
    });

    // Map normalized statuses to display names
    const unreadCount = (statusCounts.get('toread') || 0) + (statusCounts.get('unread') || 0);
    const readingCount = (statusCounts.get('reading') || 0);
    const finishedCount = (statusCounts.get('finished') || 0);

    // Recalculate average progress based on actual papers in collection
    // Each paper contributes to average based on its status:
    // - Finished: 100%
    // - Reading: average progress from workflows for reading papers (or estimate 50% if no data)
    // - ToRead/Unread: 0%
    
    // Calculate average progress for reading papers from team member progress
    let readingPapersProgressSum = 0;
    let readingPapersWithProgress = 0;
    
    if (progressData.teamMemberProgress && readingCount > 0) {
        // Get average progress for reading papers from team member data
        // We'll estimate reading papers at 50% if we can't get exact data
        // Or we can use the average from team member progress
        const allMemberProgress = progressData.teamMemberProgress
            .filter(member => member.readingCount > 0)
            .map(member => member.averageProgress);
        
        if (allMemberProgress.length > 0) {
            const avgReadingProgress = allMemberProgress.reduce((sum, p) => sum + p, 0) / allMemberProgress.length;
            readingPapersProgressSum = readingCount * avgReadingProgress;
            readingPapersWithProgress = readingCount;
        } else {
            // Estimate reading papers at 50% if no data available
            readingPapersProgressSum = readingCount * 50;
            readingPapersWithProgress = readingCount;
        }
    } else if (readingCount > 0) {
        // Estimate reading papers at 50% if no team member data
        readingPapersProgressSum = readingCount * 50;
        readingPapersWithProgress = readingCount;
    }
    
    // Calculate total progress: finished (100%) + reading (avg) + unread (0%)
    const totalProgress = (finishedCount * 100) + readingPapersProgressSum + (unreadCount * 0);
    const correctedAverageProgress = actualTotalPapers > 0 
        ? totalProgress / actualTotalPapers 
        : 0;

    // Recalculate status distribution percentages based on actual papers
    const totalPapersForPercentage = actualTotalPapers || 1;
    
    // Create status distribution from actual paper counts
    const correctedStatusDistribution = [
        {
            status: 'ToRead',
            count: unreadCount,
            percentage: (unreadCount / totalPapersForPercentage) * 100.0
        },
        {
            status: 'Reading',
            count: readingCount,
            percentage: (readingCount / totalPapersForPercentage) * 100.0
        },
        {
            status: 'Finished',
            count: finishedCount,
            percentage: (finishedCount / totalPapersForPercentage) * 100.0
        }
    ].filter(status => status.count > 0); // Only show statuses that exist

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
                        <div className="text-2xl font-bold">{actualTotalPapers}</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Team Members</CardTitle>
                        <Users className="h-4 w-4 text-muted-foreground"/>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{members?.length || progressData.totalUsers || 0}</div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Average Progress</CardTitle>
                        <BookOpen className="h-4 w-4 text-muted-foreground"/>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{correctedAverageProgress.toFixed(1)}%</div>
                        <Progress value={correctedAverageProgress} className="mt-2"/>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">Finished</CardTitle>
                        <CheckCircle2 className="h-4 w-4 text-muted-foreground"/>
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{finishedCount}</div>
                        <p className="text-xs text-muted-foreground mt-1">
                            of {actualTotalPapers} papers
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
                        {correctedStatusDistribution.map((status) => (
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

