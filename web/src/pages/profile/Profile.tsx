import { useAuth } from "@/contexts/AuthContext";
import { Helmet } from "react-helmet-async";
import Header from "@/pages/Header.tsx";
import PersonalInfoCard from "./PersonalInfoCard";
import PasswordChangeCard from "./PasswordChangeCard";
import AccountStatsCard from "./AccountStatsCard";

const Profile = () => {
    const { user, refreshUser, loading: authLoading } = useAuth();

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
                <meta name="description" content="Manage your personal information, affiliation, and account settings in LabVerse." />
                <meta property="og:title" content="Profile Settings — LabVerse" />
                <meta property="og:description" content="View and update your LabVerse profile details." />
                <meta property="og:type" content="profile" />
                <meta property="og:site_name" content="LabVerse" />
                <meta property="og:image" content="/og-profile.png" />
                <meta property="og:url" content="https://labverse.app/profile" />
                <link rel="canonical" href="https://labverse.app/profile" />
            </Helmet>

            <div className="min-h-screen bg-background">
                <Header />
                <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
                    <div className="max-w-2xl mx-auto space-y-6">
                        <div>
                            <h1 className="text-3xl font-bold mb-2">Profile Settings</h1>
                            <p className="text-muted-foreground">Manage your account information</p>
                        </div>

                        <PersonalInfoCard user={user} refreshUser={refreshUser} />
                        <PasswordChangeCard />
                        <AccountStatsCard user={user} />
                    </div>
                </main>
            </div>
        </>
    );
};

export default Profile;