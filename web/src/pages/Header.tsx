import React from 'react';
import {Link, useLocation} from "react-router-dom";
import {BookOpen, Users, BookMarked, Compass} from "lucide-react";
import {Button} from "@/components/ui/button.tsx";
import {useAuth} from "@/contexts/AuthContext.tsx";
import AppNavigation from "@/components/AppNavigation.tsx";
import NotificationBell from "@/components/NotificationBell.tsx";
import {cn} from "@/lib/utils";

const Header = () => {
    const {signOut} = useAuth();

    return (
        <>
            <header className="border-b border-border bg-background/80 backdrop-blur-sm sticky top-0 z-50">
                <div className="container mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex h-16 items-center justify-between">
                        <Link to="/dashboard"
                              className="flex items-center gap-2 transition-smooth hover:opacity-80">
                            <BookOpen className="h-6 w-6 text-primary"/>
                            <span className="text-xl font-bold text-gradient">LabVerse</span>
                        </Link>

                        <AppNavigation />

                        <div className="flex items-center gap-4">
                            <NotificationBell />
                            <Link to="/profile">
                                <Button variant="outline" size="sm">Profile</Button>
                            </Link>
                            <Button variant="ghost" size="sm" onClick={() => signOut()}>Sign Out</Button>
                        </div>
                    </div>
                </div>
            </header>
            {/* Mobile Navigation */}
            <MobileNavigation />
        </>
    );
};

const MobileNavigation = () => {
    const location = useLocation();

    const navItems = [
        { path: "/dashboard", label: "Library", icon: BookOpen },
        { path: "/collections", label: "Collections", icon: Users },
        { path: "/reading-lists", label: "Reading Lists", icon: BookMarked },
        { path: "/discover", label: "Discover", icon: Compass },
    ];

    const isActive = (path: string) => {
        if (path === "/dashboard") {
            return location.pathname === "/dashboard";
        }
        return location.pathname.startsWith(path);
    };

    return (
        <div className="md:hidden border-b border-border bg-background">
            <div className="container mx-auto px-4 py-3">
                <div className="flex gap-2 overflow-x-auto">
                    {navItems.map((item) => {
                        const Icon = item.icon;
                        const active = isActive(item.path);
                        return (
                            <Link
                                key={item.path}
                                to={item.path}
                                className="flex-shrink-0"
                            >
                                <button
                                    className={cn(
                                        "flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-medium transition-smooth",
                                        active
                                            ? "bg-primary/10 text-primary border border-primary"
                                            : "bg-muted text-muted-foreground hover:bg-muted/80"
                                    )}
                                >
                                    <Icon className="h-4 w-4" />
                                    {item.label}
                                </button>
                            </Link>
                        );
                    })}
                </div>
            </div>
        </div>
    );
};

export default Header;