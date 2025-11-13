import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {useNavigate} from "react-router-dom";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Plus, Users} from "lucide-react";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import Header from "@/pages/Header.tsx";
import {
    getTeams,
    createTeam,
    deleteTeam,
    type TeamResponse
} from "@/services/team.service";
import CreateTeamDialog from "./components/CreateTeamDialog";
import TeamsSearchFilter from "./components/TeamsSearchFilter";
import TeamsGrid from "./components/TeamsGrid";
import PaginationControls from "./components/PaginationControls";

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

    const handleSearchChange = (query: string) => {
        setSearchQuery(query);
        setPage(0);
    };

    const handlePrivacyFilterChange = (filter: 'PUBLIC' | 'PRIVATE' | '') => {
        setPrivacyFilter(filter);
        setPage(0);
    };

    const handleDelete = (team: TeamResponse) => {
        if (window.confirm(`Are you sure you want to delete "${team.name}"? This action cannot be undone.`)) {
            deleteMutation.mutate(team.id);
        }
    };

    const handleTeamClick = (team: TeamResponse) => {
        navigate(`/teams/${team.id}`);
    };

    const handlePreviousPage = () => {
        setPage(p => Math.max(0, p - 1));
    };

    const handleNextPage = () => {
        setPage(p => Math.min(totalPages - 1, p + 1));
    };

    const hasFilters = searchQuery || privacyFilter;

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

                            <CreateTeamDialog
                                open={isCreateOpen}
                                onOpenChange={setIsCreateOpen}
                                teamName={newTeam.name}
                                description={newTeam.description}
                                researchField={newTeam.researchField}
                                privacy={newTeam.privacy}
                                onNameChange={(name) => setNewTeam({...newTeam, name})}
                                onDescriptionChange={(desc) => setNewTeam({...newTeam, description: desc})}
                                onResearchFieldChange={(field) => setNewTeam({...newTeam, researchField: field})}
                                onPrivacyChange={(priv) => setNewTeam({...newTeam, privacy: priv})}
                                onSubmit={() => createMutation.mutate()}
                                isLoading={createMutation.status === 'pending'}
                            />
                        </div>

                        <TeamsSearchFilter
                            searchQuery={searchQuery}
                            privacyFilter={privacyFilter}
                            onSearchChange={handleSearchChange}
                            onPrivacyFilterChange={handlePrivacyFilterChange}
                        />

                        {isLoading ? (
                            <div className="flex justify-center py-12">
                                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                            </div>
                        ) : teams.length > 0 ? (
                            <>
                                <TeamsGrid
                                    teams={teams}
                                    isLoading={false}
                                    onDelete={handleDelete}
                                    onClick={handleTeamClick}
                                    onCreateClick={() => setIsCreateOpen(true)}
                                />

                                <PaginationControls
                                    page={page}
                                    totalPages={totalPages}
                                    onPreviousPage={handlePreviousPage}
                                    onNextPage={handleNextPage}
                                />
                            </>
                        ) : (
                            <Card className="text-center py-12">
                                <CardContent>
                                    <Users className="h-12 w-12 mx-auto mb-4 text-muted-foreground"/>
                                    <h3 className="text-lg font-semibold mb-2">No teams found</h3>
                                    <p className="text-muted-foreground mb-4">
                                        {hasFilters
                                            ? 'Try adjusting your search or filters'
                                            : 'Create your first team to start collaborating with researchers'}
                                    </p>
                                    {!hasFilters && (
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
