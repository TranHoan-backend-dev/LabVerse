import React from 'react';
import {Card, CardContent, CardDescription, CardHeader, CardTitle} from "@/components/ui/card";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import SignInForm from './SignInForm';
import SignUpForm from './SignUpForm';

type Props = {
    isLoading: boolean;
    roleName: 'PI' | 'RESEARCHER' | 'STUDENT' | '';
    onRoleChange: (role: 'PI' | 'RESEARCHER' | 'STUDENT' | '') => void;
    onSignIn: (e: React.FormEvent<HTMLFormElement>) => void;
    onSignUp: (e: React.FormEvent<HTMLFormElement>) => void;
    onForgotPasswordClick: () => void;
};

const AuthCard: React.FC<Props> = ({
    isLoading,
    roleName,
    onRoleChange,
    onSignIn,
    onSignUp,
    onForgotPasswordClick,
}) => {
    return (
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
                        <SignInForm
                            isLoading={isLoading}
                            onSubmit={onSignIn}
                            onForgotPasswordClick={onForgotPasswordClick}
                        />
                    </TabsContent>

                    <TabsContent value="signup" className="space-y-4">
                        <SignUpForm
                            isLoading={isLoading}
                            roleName={roleName}
                            onRoleChange={onRoleChange}
                            onSubmit={onSignUp}
                        />
                    </TabsContent>
                </Tabs>
            </CardContent>
        </Card>
    );
};

export default AuthCard;
