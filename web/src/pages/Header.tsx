import React from 'react';
import {Link} from "react-router-dom";
import {BookOpen} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import {useAuth} from "@/contexts/AuthContext.tsx";

const Header = () => {
    const {signOut} = useAuth();

    return (
        <header className="border-b border-border bg-background/80 backdrop-blur-sm sticky top-0 z-50">
            <div className="container mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex h-16 items-center justify-between">
                    <Link to="/dashboard"
                          className="flex items-center gap-2 transition-smooth hover:opacity-80">
                        <BookOpen className="h-6 w-6 text-primary"/>
                        <span className="text-xl font-bold text-gradient">LabVerse</span>
                    </Link>
                    <div className="flex items-center gap-4">
                        <Link to="/profile">
                            <Button variant="outline" size="sm">Profile</Button>
                        </Link>
                        <Button variant="ghost" size="sm" onClick={() => signOut()}>Sign Out</Button>
                    </div>
                </div>
            </div>
        </header>
    );
};

export default Header;