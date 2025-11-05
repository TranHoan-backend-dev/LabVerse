import {useState} from "react";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {supabase} from "@/integrations/supabase/client";
import {Link} from "react-router-dom";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle} from "@/components/ui/card";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {BookOpen, Save} from "lucide-react";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";

const Profile = () => {
    const {user, signOut} = useAuth();
    const queryClient = useQueryClient();
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({
        full_name: '',
        affiliation: '',
    });

    const {data: profile, isLoading} = useQuery({
        queryKey: ['profile', user?.id],
        queryFn: async () => {
            const {data, error} = await supabase
                .from('profiles')
                .select('*')
                .eq('id', user?.id)
                .single();

            if (error) throw error;
            setFormData({
                full_name: data.full_name || '',
                affiliation: data.affiliation || '',
            });
            return data;
        },
        enabled: !!user,
    });

    const updateMutation = useMutation({
        mutationFn: async () => {
            const {error} = await supabase
                .from('profiles')
                .update({
                    full_name: formData.full_name,
                    affiliation: formData.affiliation,
                })
                .eq('id', user?.id);

            if (error) throw error;
        },
        onSuccess: () => {
            toast.success('Profile updated successfully');
            queryClient.invalidateQueries({queryKey: ['profile']});
            setIsEditing(false);
        },
        onError: () => {
            toast.error('Failed to update profile');
        },
    });

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
            </div>
        );
    }

    return (
        <>
            <Helmet>
                <title>Profile Settings — LabVerse</title>
                <meta
                    name="description"
                    content="Manage your personal information, affiliation, and account settings in LabVerse."
                />
                <meta property="og:title" content="Profile Settings — LabVerse"/>
                <meta
                    property="og:description"
                    content="View and update your LabVerse profile details, including full name, affiliation, and account statistics."
                />
                <meta property="og:type" content="profile"/>
                <meta property="og:site_name" content="LabVerse"/>
                <meta property="og:image" content="/og-profile.png"/>
                <meta property="og:url" content="https://labverse.app/profile"/>
                <link rel="canonical" href="https://labverse.app/profile"/>
            </Helmet>
            <div className="min-h-screen bg-background">
                <header className="border-b border-border bg-background/80 backdrop-blur-sm sticky top-0 z-50">
                    <div className="container mx-auto px-4 sm:px-6 lg:px-8">
                        <div className="flex h-16 items-center justify-between">
                            <Link to="/dashboard"
                                  className="flex items-center gap-2 transition-smooth hover:opacity-80">
                                <BookOpen className="h-6 w-6 text-primary"/>
                                <span className="text-xl font-bold text-gradient">LabVerse</span>
                            </Link>

                            <Button variant="ghost" size="sm" onClick={() => signOut()}>
                                Sign Out
                            </Button>
                        </div>
                    </div>
                </header>

                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="max-w-2xl mx-auto space-y-6">
                        <div>
                            <h1 className="text-3xl font-bold mb-2">Profile Settings</h1>
                            <p className="text-muted-foreground">
                                Manage your account information
                            </p>
                        </div>

                        <Card>
                            <CardHeader>
                                <CardTitle>Personal Information</CardTitle>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                <div className="space-y-2">
                                    <Label htmlFor="email">Email</Label>
                                    <Input
                                        id="email"
                                        type="email"
                                        value={user?.email || ''}
                                        disabled
                                        className="bg-muted"
                                    />
                                    <p className="text-xs text-muted-foreground">
                                        Email cannot be changed
                                    </p>
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="full_name">Full Name</Label>
                                    <Input
                                        id="full_name"
                                        value={formData.full_name}
                                        onChange={(e) => setFormData({...formData, full_name: e.target.value})}
                                        disabled={!isEditing}
                                        placeholder="Your full name"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="affiliation">Affiliation</Label>
                                    <Input
                                        id="affiliation"
                                        value={formData.affiliation}
                                        onChange={(e) => setFormData({...formData, affiliation: e.target.value})}
                                        disabled={!isEditing}
                                        placeholder="Your university or lab"
                                    />
                                </div>

                                <div className="flex gap-2">
                                    {!isEditing ? (
                                        <Button onClick={() => setIsEditing(true)}>
                                            Edit Profile
                                        </Button>
                                    ) : (
                                        <>
                                            <Button
                                                onClick={() => updateMutation.mutate()}
                                                disabled={updateMutation.isPending}
                                            >
                                                <Save className="h-4 w-4 mr-2"/>
                                                {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
                                            </Button>
                                            <Button
                                                variant="outline"
                                                onClick={() => {
                                                    setIsEditing(false);
                                                    setFormData({
                                                        full_name: profile?.full_name || '',
                                                        affiliation: profile?.affiliation || '',
                                                    });
                                                }}
                                            >
                                                Cancel
                                            </Button>
                                        </>
                                    )}
                                </div>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Account Statistics</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-2 text-sm">
                                    <p><strong>Account
                                        created:</strong> {new Date(profile?.created_at).toLocaleDateString()}</p>
                                    <p><strong>Last
                                        updated:</strong> {new Date(profile?.updated_at).toLocaleDateString()}</p>
                                </div>
                            </CardContent>
                        </Card>
                    </div>
                </main>
            </div>
        </>
    );
};

export default Profile;
