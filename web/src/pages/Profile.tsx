import {useState, useEffect} from "react";
import {useMutation} from "@tanstack/react-query";
import {Button} from "@/components/ui/button";
import {Card, CardContent, CardHeader, CardTitle, CardDescription} from "@/components/ui/card";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Save, Lock, Eye, EyeOff} from "lucide-react";
import {toast} from "sonner";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import Header from "@/pages/Header.tsx";
import * as accountService from "@/services/account.service";

const Profile = () => {
    const {user, refreshUser, loading: authLoading} = useAuth();
    const [isEditing, setIsEditing] = useState(false);
    const [isEditingPassword, setIsEditingPassword] = useState(false);
    const [showCurrentPassword, setShowCurrentPassword] = useState(false);
    const [showNewPassword, setShowNewPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    
    const [formData, setFormData] = useState({
        fullName: '',
        username: '',
        avatarUrl: '',
    });

    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });

    useEffect(() => {
        if (user) {
            setFormData({
                fullName: user.fullName || '',
                username: user.username || '',
                avatarUrl: user.avatarUrl || '',
            });
        }
    }, [user]);

    const updateMutation = useMutation({
        mutationFn: async () => {
            // Validate form data
            if (!formData.fullName.trim()) {
                throw new Error('Full name is required');
            }
            if (!formData.username.trim()) {
                throw new Error('Username is required');
            }
            if (formData.username.length < 3 || formData.username.length > 50) {
                throw new Error('Username must be between 3 and 50 characters');
            }

            const updateData: {
                fullName: string;
                username: string;
                avatarUrl?: string;
            } = {
                fullName: formData.fullName.trim(),
                username: formData.username.trim(),
            };

            // Only include avatarUrl if it's not empty
            if (formData.avatarUrl.trim()) {
                updateData.avatarUrl = formData.avatarUrl.trim();
            }

            await accountService.updateProfile(updateData);
        },
        onSuccess: async () => {
            toast.success('Profile updated successfully');
            await refreshUser();
            setIsEditing(false);
        },
        onError: (error: any) => {
            toast.error(error.message || 'Failed to update profile');
        },
    });

    const changePasswordMutation = useMutation({
        mutationFn: async () => {
            // Validate password data
            if (!passwordData.currentPassword) {
                throw new Error('Current password is required');
            }
            if (!passwordData.newPassword) {
                throw new Error('New password is required');
            }
            if (passwordData.newPassword.length < 6) {
                throw new Error('New password must be at least 6 characters');
            }
            if (passwordData.newPassword !== passwordData.confirmPassword) {
                throw new Error('New passwords do not match');
            }
            if (passwordData.currentPassword === passwordData.newPassword) {
                throw new Error('New password must be different from current password');
            }

            await accountService.changePassword({
                currentPassword: passwordData.currentPassword,
                newPassword: passwordData.newPassword,
            });
        },
        onSuccess: () => {
            toast.success('Password changed successfully');
            setIsEditingPassword(false);
            setPasswordData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: '',
            });
        },
        onError: (error: any) => {
            toast.error(error.message || 'Failed to change password');
        },
    });

    if (authLoading) {
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
                <Header/>

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
                                    <Label htmlFor="username">Username</Label>
                                    <Input
                                        id="username"
                                        value={formData.username}
                                        onChange={(e) => setFormData({...formData, username: e.target.value})}
                                        disabled={!isEditing}
                                        placeholder="Your username"
                                        minLength={3}
                                        maxLength={50}
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="fullName">Full Name</Label>
                                    <Input
                                        id="fullName"
                                        value={formData.fullName}
                                        onChange={(e) => setFormData({...formData, fullName: e.target.value})}
                                        disabled={!isEditing}
                                        placeholder="Your full name"
                                    />
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="avatarUrl">Avatar URL</Label>
                                    <Input
                                        id="avatarUrl"
                                        value={formData.avatarUrl}
                                        onChange={(e) => setFormData({...formData, avatarUrl: e.target.value})}
                                        disabled={!isEditing}
                                        placeholder="https://example.com/avatar.jpg"
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
                                                        fullName: user?.fullName || '',
                                                        username: user?.username || '',
                                                        avatarUrl: user?.avatarUrl || '',
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
                                <CardTitle>Change Password</CardTitle>
                                <CardDescription>
                                    Update your password to keep your account secure
                                </CardDescription>
                            </CardHeader>
                            <CardContent className="space-y-4">
                                {!isEditingPassword ? (
                                    <Button 
                                        variant="outline" 
                                        onClick={() => setIsEditingPassword(true)}
                                    >
                                        <Lock className="h-4 w-4 mr-2"/>
                                        Change Password
                                    </Button>
                                ) : (
                                    <>
                                        <div className="space-y-2">
                                            <Label htmlFor="currentPassword">Current Password</Label>
                                            <div className="relative">
                                                <Input
                                                    id="currentPassword"
                                                    type={showCurrentPassword ? "text" : "password"}
                                                    value={passwordData.currentPassword}
                                                    onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})}
                                                    placeholder="Enter your current password"
                                                    required
                                                />
                                                <Button
                                                    type="button"
                                                    variant="ghost"
                                                    size="sm"
                                                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                                                    onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                                                >
                                                    {showCurrentPassword ? (
                                                        <EyeOff className="h-4 w-4 text-muted-foreground" />
                                                    ) : (
                                                        <Eye className="h-4 w-4 text-muted-foreground" />
                                                    )}
                                                </Button>
                                            </div>
                                        </div>

                                        <div className="space-y-2">
                                            <Label htmlFor="newPassword">New Password</Label>
                                            <div className="relative">
                                                <Input
                                                    id="newPassword"
                                                    type={showNewPassword ? "text" : "password"}
                                                    value={passwordData.newPassword}
                                                    onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})}
                                                    placeholder="Enter your new password"
                                                    required
                                                    minLength={6}
                                                />
                                                <Button
                                                    type="button"
                                                    variant="ghost"
                                                    size="sm"
                                                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                                                    onClick={() => setShowNewPassword(!showNewPassword)}
                                                >
                                                    {showNewPassword ? (
                                                        <EyeOff className="h-4 w-4 text-muted-foreground" />
                                                    ) : (
                                                        <Eye className="h-4 w-4 text-muted-foreground" />
                                                    )}
                                                </Button>
                                            </div>
                                            <p className="text-xs text-muted-foreground">
                                                Password must be at least 6 characters
                                            </p>
                                        </div>

                                        <div className="space-y-2">
                                            <Label htmlFor="confirmPassword">Confirm New Password</Label>
                                            <div className="relative">
                                                <Input
                                                    id="confirmPassword"
                                                    type={showConfirmPassword ? "text" : "password"}
                                                    value={passwordData.confirmPassword}
                                                    onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})}
                                                    placeholder="Confirm your new password"
                                                    required
                                                    minLength={6}
                                                />
                                                <Button
                                                    type="button"
                                                    variant="ghost"
                                                    size="sm"
                                                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                                >
                                                    {showConfirmPassword ? (
                                                        <EyeOff className="h-4 w-4 text-muted-foreground" />
                                                    ) : (
                                                        <Eye className="h-4 w-4 text-muted-foreground" />
                                                    )}
                                                </Button>
                                            </div>
                                        </div>

                                        <div className="flex gap-2">
                                            <Button
                                                onClick={() => changePasswordMutation.mutate()}
                                                disabled={changePasswordMutation.isPending}
                                            >
                                                <Lock className="h-4 w-4 mr-2"/>
                                                {changePasswordMutation.isPending ? 'Changing Password...' : 'Change Password'}
                                            </Button>
                                            <Button
                                                variant="outline"
                                                onClick={() => {
                                                    setIsEditingPassword(false);
                                                    setPasswordData({
                                                        currentPassword: '',
                                                        newPassword: '',
                                                        confirmPassword: '',
                                                    });
                                                }}
                                                disabled={changePasswordMutation.isPending}
                                            >
                                                Cancel
                                            </Button>
                                        </div>
                                    </>
                                )}
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Account Statistics</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-2 text-sm">
                                    <p><strong>Role:</strong> {user?.role || 'N/A'}</p>
                                    {user?.createdDate && (
                                        <p><strong>Account created:</strong> {new Date(user.createdDate).toLocaleDateString()}</p>
                                    )}
                                    {user?.updatedDate && (
                                        <p><strong>Last updated:</strong> {new Date(user.updatedDate).toLocaleDateString()}</p>
                                    )}
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
