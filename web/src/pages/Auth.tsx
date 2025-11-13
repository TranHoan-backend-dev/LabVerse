import {useState, useEffect} from "react";
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Separator} from "@/components/ui/separator";
import {BookOpen} from "lucide-react";
import {Link, useNavigate} from "react-router-dom";
import {useAuth} from "@/contexts/AuthContext";
import {Helmet} from "react-helmet-async";
import {GoogleSignInButton} from "@/components/GoogleSignInButton";
import {ForgotPasswordDialog} from "@/components/ForgotPasswordDialog";
import {OtpVerificationDialog} from "@/components/OtpVerificationDialog";

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
                    <Link to="/" className="flex items-center justify-center gap-2 transition-smooth hover:opacity-80">
                        <BookOpen className="h-8 w-8 text-primary"/>
                        <span className="text-2xl font-bold text-gradient">LabVerse</span>
                    </Link>

                    <Card className="shadow-custom-lg border-border">
                        <CardHeader className="space-y-1">
                            <CardTitle className="text-2xl text-center">Welcome back</CardTitle>
                            <CardDescription className="text-center">
                                Sign in to your account or create a new one
                            </CardDescription>
                        </CardHeader>
                        <CardContent>
                            <Tabs defaultValue="signin" className="w-full">
                                <TabsList className="grid w-full grid-cols-2">
                                    <TabsTrigger value="signin">Sign In</TabsTrigger>
                                    <TabsTrigger value="signup">Sign Up</TabsTrigger>
                                </TabsList>

                                <TabsContent value="signin" className="space-y-4">
                                    <GoogleSignInButton isLoading={isLoading} disabled={isLoading} />
                                    <div className="relative">
                                        <div className="absolute inset-0 flex items-center">
                                            <Separator />
                                        </div>
                                        <div className="relative flex justify-center text-xs uppercase">
                                            <span className="bg-background px-2 text-muted-foreground">
                                                Or continue with email
                                            </span>
                                        </div>
                                    </div>
                                    <form onSubmit={handleSignIn} className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="signin-email">Email</Label>
                                            <Input
                                                id="signin-email"
                                                name="email"
                                                type="email"
                                                placeholder="name@example.com"
                                                required
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <div className="flex items-center justify-between">
                                                <Label htmlFor="signin-password">Password</Label>
                                                <button
                                                    type="button"
                                                    onClick={(e) => {
                                                        e.preventDefault();
                                                        const emailInput = document.getElementById('signin-email') as HTMLInputElement;
                                                        if (emailInput && emailInput.value) {
                                                            setForgotPasswordEmail(emailInput.value);
                                                        }
                                                        setShowForgotPassword(true);
                                                    }}
                                                    className="text-sm text-primary hover:underline"
                                                >
                                                    Forgot password?
                                                </button>
                                            </div>
                                            <Input
                                                id="signin-password"
                                                name="password"
                                                type="password"
                                                placeholder="••••••••"
                                                required
                                            />
                                        </div>
                                        <Button type="submit" className="w-full" disabled={isLoading}>
                                            {isLoading ? "Signing in..." : "Sign In"}
                                        </Button>
                                    </form>
                                </TabsContent>

                                <TabsContent value="signup" className="space-y-4">
                                    <GoogleSignInButton isLoading={isLoading} disabled={isLoading} />
                                    <div className="relative">
                                        <div className="absolute inset-0 flex items-center">
                                            <Separator />
                                        </div>
                                        <div className="relative flex justify-center text-xs uppercase">
                                            <span className="bg-background px-2 text-muted-foreground">
                                                Or continue with email
                                            </span>
                                        </div>
                                    </div>
                                    <form onSubmit={handleSignUp} className="space-y-4">
                                        <div className="space-y-2">
                                            <Label htmlFor="signup-name">Full Name</Label>
                                            <Input
                                                id="signup-name"
                                                name="fullName"
                                                type="text"
                                                placeholder="John Doe"
                                                required
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="signup-username">Username</Label>
                                            <Input
                                                id="signup-username"
                                                name="username"
                                                type="text"
                                                placeholder="johndoe"
                                                required
                                                minLength={3}
                                                maxLength={50}
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="signup-email">Email</Label>
                                            <Input
                                                id="signup-email"
                                                name="email"
                                                type="email"
                                                placeholder="name@example.com"
                                                required
                                            />
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="signup-role">Role</Label>
                                            <Select value={roleName} onValueChange={(value) => setRoleName(value as 'PI' | 'RESEARCHER' | 'STUDENT')} required>
                                                <SelectTrigger id="signup-role">
                                                    <SelectValue placeholder="Select your role" />
                                                </SelectTrigger>
                                                <SelectContent>
                                                    <SelectItem value="STUDENT">Student</SelectItem>
                                                    <SelectItem value="RESEARCHER">Researcher</SelectItem>
                                                    <SelectItem value="PI">Principal Investigator (PI)</SelectItem>
                                                </SelectContent>
                                            </Select>
                                        </div>
                                        <div className="space-y-2">
                                            <Label htmlFor="signup-password">Password</Label>
                                            <Input
                                                id="signup-password"
                                                name="password"
                                                type="password"
                                                placeholder="••••••••"
                                                required
                                                minLength={6}
                                            />
                                        </div>
                                        <Button type="submit" className="w-full" disabled={isLoading}>
                                            {isLoading ? "Creating account..." : "Create Account"}
                                        </Button>
                                    </form>
                                </TabsContent>
                            </Tabs>
                        </CardContent>
                    </Card>
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
