// src/pages/profile/components/PersonalInfoCard.tsx
import { useState, useEffect } from "react";
import { useMutation } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Save } from "lucide-react";
import { toast } from "sonner";
import * as accountService from "@/services/account.service";
import EditableField from "./components/EditableField";
import AvatarPreview from "./components/AvatarPreview";

interface User {
    fullName?: string;
    username?: string;
    avatarUrl?: string;
    email?: string;
}

interface PersonalInfoCardProps {
    user: User | null;
    refreshUser: () => Promise<void>;
}

const PersonalInfoCard = ({ user, refreshUser }: PersonalInfoCardProps) => {
    const [isEditing, setIsEditing] = useState(false);
    const [formData, setFormData] = useState({
        fullName: '',
        username: '',
        avatarUrl: '',
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
            if (!formData.fullName.trim()) throw new Error('Full name is required');
            if (!formData.username.trim()) throw new Error('Username is required');
            if (formData.username.length < 3 || formData.username.length > 50)
                throw new Error('Username must be between 3 and 50 characters');

            const updateData: any = {
                fullName: formData.fullName.trim(),
                username: formData.username.trim(),
            };
            if (formData.avatarUrl.trim()) updateData.avatarUrl = formData.avatarUrl.trim();

            await accountService.updateProfile(updateData);
        },
        onSuccess: async () => {
            toast.success('Profile updated successfully');
            await refreshUser();
            setIsEditing(false);
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to update profile');
        },
    });

    const handleCancel = () => {
        setFormData({
            fullName: user?.fullName || '',
            username: user?.username || '',
            avatarUrl: user?.avatarUrl || '',
        });
        setIsEditing(false);
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>Personal Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
                {/* Avatar Preview */}
                <div className="flex justify-center">
                    <AvatarPreview
                        src={formData.avatarUrl}
                        alt={formData.fullName || formData.username}
                        size="lg"
                        fallback={formData.username?.[0]?.toUpperCase() || 'U'}
                    />
                </div>

                <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input id="email" type="email" value={user?.email || ''} disabled className="bg-muted" />
                    <p className="text-xs text-muted-foreground">Email cannot be changed</p>
                </div>

                <EditableField
                    label="Username"
                    value={formData.username}
                    onChange={(v) => setFormData({ ...formData, username: v })}
                    disabled={!isEditing}
                    placeholder="Your username"
                    minLength={3}
                    maxLength={50}
                />

                <EditableField
                    label="Full Name"
                    value={formData.fullName}
                    onChange={(v) => setFormData({ ...formData, fullName: v })}
                    disabled={!isEditing}
                    placeholder="Your full name"
                />

                <div className="space-y-2">
                    <Label htmlFor="avatarUrl">Avatar URL</Label>
                    <Input
                        id="avatarUrl"
                        value={formData.avatarUrl}
                        onChange={(e) => setFormData({ ...formData, avatarUrl: e.target.value })}
                        disabled={!isEditing}
                        placeholder="https://example.com/avatar.jpg"
                    />
                    <p className="text-xs text-muted-foreground">
                        Paste a direct image link (supports PNG, JPG, WebP)
                    </p>
                </div>

                <div className="flex gap-2">
                    {!isEditing ? (
                        <Button onClick={() => setIsEditing(true)}>Edit Profile</Button>
                    ) : (
                        <>
                            <Button onClick={() => updateMutation.mutate()} disabled={updateMutation.isPending}>
                                <Save className="h-4 w-4 mr-2" />
                                {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
                            </Button>
                            <Button variant="outline" onClick={handleCancel}>
                                Cancel
                            </Button>
                        </>
                    )}
                </div>
            </CardContent>
        </Card>
    );
};

export default PersonalInfoCard;