import React, {useState} from 'react';
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Separator} from "@/components/ui/separator";
import {GoogleSignInButton} from "@/components/GoogleSignInButton";
import {Eye, EyeOff} from "lucide-react";

type Props = {
    isLoading: boolean;
    onSubmit: (e: React.FormEvent<HTMLFormElement>) => void;
    onForgotPasswordClick: () => void;
};

const SignInForm: React.FC<Props> = ({isLoading, onSubmit, onForgotPasswordClick}) => {
    const [showPassword, setShowPassword] = useState(false);

    return (
        <div className="space-y-4">
            <GoogleSignInButton isLoading={isLoading} disabled={isLoading}/>

            <div className="relative">
                <div className="absolute inset-0 flex items-center">
                    <Separator/>
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                    <span className="bg-background px-2 text-muted-foreground">
                        Or continue with email
                    </span>
                </div>
            </div>

            <form onSubmit={onSubmit} className="space-y-4">
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
                            onClick={(e) => onForgotPasswordClick}
                            className="text-sm text-primary hover:underline"
                        >
                            Forgot password?
                        </button>
                    </div>

                    <div className="relative">
                        <Input
                            id="signin-password"
                            name="password"
                            type={showPassword ? "text" : "password"}
                            placeholder="Enter your password"
                            required
                        />

                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute inset-y-0 right-3 flex items-center text-muted-foreground hover:text-foreground"
                        >
                            {showPassword ? <EyeOff size={18}/> : <Eye size={18}/>}
                        </button>
                    </div>
                </div>

                <Button type="submit" className="w-full" disabled={isLoading}>
                    {isLoading ? "Signing in..." : "Sign In"}
                </Button>
            </form>
        </div>
    );
};

export default SignInForm;