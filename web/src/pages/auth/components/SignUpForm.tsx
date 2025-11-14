import React from 'react';
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Separator} from "@/components/ui/separator";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {GoogleSignInButton} from "@/components/GoogleSignInButton";

type Props = {
    isLoading: boolean;
    roleName: 'PI' | 'RESEARCHER' | 'STUDENT' | '';
    onRoleChange: (role: 'PI' | 'RESEARCHER' | 'STUDENT' | '') => void;
    onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
};

const SignUpForm: React.FC<Props> = ({isLoading, roleName, onRoleChange, onSubmit}) => {
    return (
        <div className="space-y-4">
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
            <form onSubmit={onSubmit} className="space-y-4">
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
                    <Select 
                        value={roleName} 
                        onValueChange={(value) => onRoleChange(value as 'PI' | 'RESEARCHER' | 'STUDENT')} 
                        required
                    >
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
        </div>
    );
};

export default SignUpForm;
