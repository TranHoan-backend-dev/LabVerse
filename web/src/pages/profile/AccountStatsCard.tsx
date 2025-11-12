// src/pages/profile/components/AccountStatsCard.tsx
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface User {
    role?: string;
    createdDate?: string;
    updatedDate?: string;
}

interface AccountStatsCardProps {
    user: User | null;
}

const AccountStatsCard = ({ user }: AccountStatsCardProps) => {
    return (
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
    );
};

export default AccountStatsCard;