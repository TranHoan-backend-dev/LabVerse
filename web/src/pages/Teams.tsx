import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {useNavigate} from "react-router-dom";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "@/components/ui/dialog";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Textarea} from "@/components/ui/textarea";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Plus, Users, Edit, Trash2, MoreVertical, Search, Globe, Lock} from "lucide-react";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import Header from "@/components/Header";
import {
    getTeams,
    createTeam,
    deleteTeam,
} from "@/services/team.service";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { TeamResponse } from "@/types/team.types";

const Teams = () => {
    const {user} = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [searchQuery, setSearchQuery] = useState("");
    const [privacyFilter, setPrivacyFilter] = useState<'PUBLIC' | 'PRIVATE' | ''>('');
    const [newTeam, setNewTeam] = useState({
        name: '',
        description: '',
        researchField: '',
        privacy: 'PUBLIC' as 'PUBLIC' | 'PRIVATE'
    });
    const [page, setPage] = useState(0);
    const pageSize = 12;

    const {data: teamsData, isLoading} = useQuery({
        queryKey: ['teams', searchQuery, privacyFilter, page],
        queryFn: async () => {
            return await getTeams(
                searchQuery || undefined,
                undefined,
                privacyFilter || undefined,
                page,
                pageSize
            );
        },
    });

    const teams = teamsData?.content || [];
    const totalPages = teamsData?.totalPages || 0;

    const createMutation = useMutation({
        mutationFn: async () => {
            if (!user?.id) throw new Error('User not logged in');
            return await createTeam({
                name: newTeam.name,
                description: newTeam.description || undefined,
                researchField: newTeam.researchField || undefined,
                privacy: newTeam.privacy,
                userId: user.id
            });
        },
        onSuccess: () => {
            toast.success('Team created successfully');
            queryClient.invalidateQueries({queryKey: ['teams']});
            setIsCreateOpen(false);
            setNewTeam({
                name: '',
                description: '',
                researchField: '',
                privacy: 'PUBLIC'
            });
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to create team');
        },
    });

    const deleteMutation = useMutation({
        mutationFn: async (teamId: string) => {
            return await deleteTeam(teamId);
        },
        onSuccess: () => {
            toast.success('Team deleted successfully');
            queryClient.invalidateQueries({queryKey: ['teams']});
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to delete team');
        },
    });

    const handleDelete = (team: TeamResponse) => {
        if (window.confirm(`Are you sure you want to delete "${team.name}"? This action cannot be undone.`)) {
            deleteMutation.mutate(team.id);
        }
    };

    const handleTeamClick = (team: TeamResponse) => {
        navigate(`/teams/${team.id}`);
    };

    return (
        <>
            <Helmet>
                <title>Teams | LabVerse</title>
                <meta
                    name="description"
                    content="Join and create research teams on LabVerse. Collaborate with researchers, share papers, and work together on projects."
                />
                <meta property="og:title" content="Teams | LabVerse"/>
                <meta
                    property="og:description"
                    content="Discover and join research teams, or create your own team to collaborate on research projects."
                />
                <meta property="og:type" content="website"/>
                <meta property="og:url" content="https://labverse.app/teams"/>
            </Helmet>
            <div className="min-h-screen bg-background">
                <Header/>

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="space-y-8">
                        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                            <div>
                                <h1 className="text-3xl font-bold mb-2">Research Teams</h1>
                                <p className="text-muted-foreground">
                                    Join teams or create your own research group
                                </p>
                            </div>

                            <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
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
                                                value={newTeam.name}
                                                onChange={(e) => setNewTeam({...newTeam, name: e.target.value})}
                                                placeholder="e.g., Machine Learning Research Group"
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="description">Description</Label>
                                            <Textarea
                                                id="description"
                                                value={newTeam.description}
                                                onChange={(e) => setNewTeam({...newTeam, description: e.target.value})}
                                                placeholder="What is this team about?"
                                                rows={4}
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="research-field">Research Field</Label>
                                            <Input
                                                id="research-field"
                                                value={newTeam.researchField}
                                                onChange={(e) => setNewTeam({...newTeam, researchField: e.target.value})}
                                                placeholder="e.g., Artificial Intelligence, Bioinformatics"
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="privacy">Privacy *</Label>
                                            <Select
                                                value={newTeam.privacy}
                                                onValueChange={(value: 'PUBLIC' | 'PRIVATE') => 
                                                    setNewTeam({...newTeam, privacy: value})
                                                }
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
                                            onClick={() => createMutation.mutate()}
                                            disabled={!newTeam.name || createMutation.isPending}
                                            className="w-full"
                                        >
                                            {createMutation.isPending ? 'Creating...' : 'Create Team'}
                                        </Button>
                                    </div>
                                </DialogContent>
                            </Dialog>
                        </div>

                        {/* Search and Filter */}
                        <div className="flex flex-col sm:flex-row gap-4">
                            <div className="relative flex-1">
                                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground"/>
                                <Input
                                    placeholder="Search teams..."
                                    value={searchQuery}
                                    onChange={(e) => {
                                        setSearchQuery(e.target.value);
                                        setPage(0);
                                    }}
                                    className="pl-10"
                                />
                            </div>
                            <Select
                                value={privacyFilter === '' ? 'ALL' : privacyFilter}
                                onValueChange={(value: string) => {
                                    setPrivacyFilter(value === 'ALL' ? '' : (value as 'PUBLIC' | 'PRIVATE'));
                                    setPage(0);
                                }}
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

                        {isLoading ? (
                            <div className="flex justify-center py-12">
                                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                            </div>
                        ) : teams.length > 0 ? (
                            <>
                                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                                    {teams.map((team: TeamResponse) => (
                                        <Card
                                            key={team.id}
                                            className="shadow-custom-sm hover:shadow-custom-md transition-shadow cursor-pointer"
                                            onClick={() => handleTeamClick(team)}
                                        >
                                            <CardHeader>
                                                <CardTitle className="flex items-start justify-between">
                                                    <span className="line-clamp-2 flex-1">{team.name}</span>
                                                    <DropdownMenu>
                                                        <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                                                            <Button variant="ghost" size="icon" className="h-8 w-8">
                                                                <MoreVertical className="h-4 w-4"/>
                                                            </Button>
                                                        </DropdownMenuTrigger>
                                                        <DropdownMenuContent align="end">
                                                            <DropdownMenuItem
                                                                onClick={(e) => {
                                                                    e.stopPropagation();
                                                                    handleDelete(team);
                                                                }}
                                                                className="text-destructive"
                                                            >
                                                                <Trash2 className="h-4 w-4 mr-2"/>
                                                                Delete
                                                            </DropdownMenuItem>
                                                        </DropdownMenuContent>
                                                    </DropdownMenu>
                                                </CardTitle>
                                                {team.description && (
                                                    <CardDescription className="line-clamp-2">
                                                        {team.description}
                                                    </CardDescription>
                                                )}
                                            </CardHeader>
                                            <CardContent>
                                                <div className="text-sm text-muted-foreground space-y-2">
                                                    <div className="flex items-center gap-2">
                                                        {team.privacy === 'PUBLIC' ? (
                                                            <Globe className="h-4 w-4"/>
                                                        ) : (
                                                            <Lock className="h-4 w-4"/>
                                                        )}
                                                        <span>{team.privacy}</span>
                                                    </div>
                                                    {team.researchField && (
                                                        <p>Field: {team.researchField}</p>
                                                    )}
                                                    <p>Members: {team.memberCount || 0}</p>
                                                    <p>Papers: {team.paperCount || 0}</p>
                                                </div>
                                            </CardContent>
                                        </Card>
                                    ))}
                                </div>

                                {/* Pagination */}
                                {totalPages > 1 && (
                                    <div className="flex justify-center gap-2">
                                        <Button
                                            variant="outline"
                                            onClick={() => setPage(p => Math.max(0, p - 1))}
                                            disabled={page === 0}
                                        >
                                            Previous
                                        </Button>
                                        <span className="flex items-center px-4">
                                            Page {page + 1} of {totalPages}
                                        </span>
                                        <Button
                                            variant="outline"
                                            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                            disabled={page >= totalPages - 1}
                                        >
                                            Next
                                        </Button>
                                    </div>
                                )}
                            </>
                        ) : (
                            <Card className="text-center py-12">
                                <CardContent>
                                    <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                                    <h3 className="text-lg font-semibold mb-2">No teams found</h3>
                                    <p className="text-muted-foreground mb-4">
                                        {searchQuery || privacyFilter
                                            ? 'Try adjusting your search or filters'
                                            : 'Create your first team to start collaborating with researchers'}
                                    </p>
                                    {!searchQuery && !privacyFilter && (
                                        <Button onClick={() => setIsCreateOpen(true)}>
                                            <Plus className="h-4 w-4 mr-2"/>
                                            Create Team
                                        </Button>
                                    )}
                                </CardContent>
                            </Card>
                        )}
                    </div>
                </main>
            </div>
        </>
    );
};

export default Teams;

