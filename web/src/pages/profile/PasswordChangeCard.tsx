// src/pages/profile/components/PasswordChangeCard.tsx
import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card";
import { Lock } from "lucide-react";
import { toast } from "sonner";
import * as accountService from "@/services/account.service";
import PasswordInput from "./components/PasswordInput";

const PasswordChangeCard = () => {
    const [isEditing, setIsEditing] = useState(false);
    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
    });

    const changePasswordMutation = useMutation({
        mutationFn: async () => {
            if (!passwordData.currentPassword) throw new Error('Current password is required');
            if (!passwordData.newPassword) throw new Error('New password is required');
            if (passwordData.newPassword.length < 6) throw new Error('New password must be at least 6 characters');
            if (passwordData.newPassword !== passwordData.confirmPassword)
                throw new Error('New passwords do not match');
            if (passwordData.currentPassword === passwordData.newPassword)
                throw new Error('New password must be different from current password');

            await accountService.changePassword({
                currentPassword: passwordData.currentPassword,
                newPassword: passwordData.newPassword,
            });
        },
        onSuccess: () => {
            toast.success('Password changed successfully');
            setIsEditing(false);
            setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to change password');
        },
    });

    const handleCancel = () => {
        setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
        setIsEditing(false);
    };

    return (
        <Card>
            <CardHeader>
                <CardTitle>Change Password</CardTitle>
                <CardDescription>Update your password to keep your account secure</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
                {!isEditing ? (
                    <Button variant="outline" onClick={() => setIsEditing(true)}>
                        <Lock className="h-4 w-4 mr-2" />
                        Change Password
                    </Button>
                ) : (
                    <>
                        <PasswordInput
                            label="Current Password"
                            value={passwordData.currentPassword}
                            onChange={(v) => setPasswordData({ ...passwordData, currentPassword: v })}
                            placeholder="Enter your current password"
                        />

                        <PasswordInput
                            label="New Password"
                            value={passwordData.newPassword}
                            onChange={(v) => setPasswordData({ ...passwordData, newPassword: v })}
                            placeholder="Enter your new password"
                            minLength={6}
                            helperText="Password must be at least 6 characters"
                        />

                        <PasswordInput
                            label="Confirm New Password"
                            value={passwordData.confirmPassword}
                            onChange={(v) => setPasswordData({ ...passwordData, confirmPassword: v })}
                            placeholder="Confirm your new password"
                            minLength={6}
                        />

                        <div className="flex gap-2">
                            <Button
                                onClick={() => changePasswordMutation.mutate()}
                                disabled={changePasswordMutation.isPending}
                            >
                                <Lock className="h-4 w-4 mr-2" />
                                {changePasswordMutation.isPending ? 'Changing...' : 'Change Password'}
                            </Button>
                            <Button variant="outline" onClick={handleCancel} disabled={changePasswordMutation.isPending}>
                                Cancel
                            </Button>
                        </div>
                    </>
                )}
            </CardContent>
        </Card>
    );
};

export default PasswordChangeCard;