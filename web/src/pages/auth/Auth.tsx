import {useState, useEffect} from "react";
import {useNavigate} from "react-router-dom";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import {ForgotPasswordDialog} from "@/components/ForgotPasswordDialog";
import {OtpVerificationDialog} from "@/components/OtpVerificationDialog";
import AuthHeader from "./components/AuthHeader";
import AuthCard from "./components/AuthCard";

const Auth = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [roleName, setRoleName] = useState<'PI' | 'RESEARCHER' | 'STUDENT' | ''>('');
    const [showForgotPassword, setShowForgotPassword] = useState(false);
    const [forgotPasswordEmail, setForgotPasswordEmail] = useState("");
    const [showOtpDialog, setShowOtpDialog] = useState(false);
    const [pendingEmail, setPendingEmail] = useState<string>("");
    const {signIn, signUp, user} = useAuth();
    const navigate = useNavigate();

    // Redirect if already logged in
    useEffect(() => {
        if (user) {
            navigate('/dashboard');
        }
    }, [user, navigate]);

    const handleSignIn = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsLoading(true);

        const formData = new FormData(e.currentTarget);
        const email = formData.get('email') as string;
        const password = formData.get('password') as string;

        try {
            await signIn(email, password);
        } catch (error) {
            // Error handled in context
        } finally {
            setIsLoading(false);
        }
    };

    const handleSignUp = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsLoading(true);

        const formData = new FormData(e.currentTarget);
        const email = formData.get('email') as string;
        const password = formData.get('password') as string;
        const fullName = formData.get('fullName') as string;
        const username = formData.get('username') as string;

        if (!roleName) {
            alert('Please select a role');
            setIsLoading(false);
            return;
        }

        try {
            const registeredEmail = await signUp(email, password, fullName, username, roleName as 'PI' | 'RESEARCHER' | 'STUDENT');
            // Show OTP dialog after successful registration
            setPendingEmail(registeredEmail);
            setShowOtpDialog(true);
        } catch (error) {
            // Error handled in context
        } finally {
            setIsLoading(false);
        }
    };

    const handleForgotPasswordClick = () => {
        const emailInput = document.getElementById('signin-email') as HTMLInputElement;
        if (emailInput && emailInput.value) {
            setForgotPasswordEmail(emailInput.value);
        }
        setShowForgotPassword(true);
    };

    return (
        <>
            <Helmet>
                <title>LabVerse | Sign In & Sign Up</title>
                <meta
                    name="description"
                    content="Sign in or create a new account on LabVerse to manage your research, papers, and collaborations easily."
                />
                <meta property="og:title" content="LabVerse - Sign In & Sign Up"/>
                <meta
                    property="og:description"
                    content="Join LabVerse — the platform for scientific research and knowledge sharing."
                />
                <meta property="og:image" content="/og-auth.png"/>
                <meta property="og:type" content="website"/>
                <meta property="og:url" content="https://labverse.app/auth"/>
                <link rel="canonical" href="https://labverse.app/auth"/>
            </Helmet>
            <div
                className="min-h-screen flex items-center justify-center p-4 bg-gradient-to-br from-primary/5 via-background to-accent/5">
                <div className="w-full max-w-md space-y-6">
                    <AuthHeader />

                    <AuthCard
                        isLoading={isLoading}
                        roleName={roleName}
                        onRoleChange={setRoleName}
                        onSignIn={handleSignIn}
                        onSignUp={handleSignUp}
                        onForgotPasswordClick={handleForgotPasswordClick}
                    />
                </div>
            </div>
            <ForgotPasswordDialog
                open={showForgotPassword}
                initialEmail={forgotPasswordEmail}
                onClose={() => {
                    setShowForgotPassword(false);
                    setForgotPasswordEmail("");
                }}
            />
            <OtpVerificationDialog
                open={showOtpDialog}
                email={pendingEmail}
                onVerified={() => {
                    setShowOtpDialog(false);
                    setPendingEmail("");
                }}
                onClose={() => {
                    setShowOtpDialog(false);
                    setPendingEmail("");
                }}
            />
        </>
    );
};

export default Auth;
