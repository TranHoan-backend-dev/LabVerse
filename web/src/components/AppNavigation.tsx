import { Link, useLocation } from "react-router-dom";
import { BookOpen, Users, BookMarked, Compass, UserPlus } from "lucide-react";
import { cn } from "@/lib/utils";

const AppNavigation = () => {
    const location = useLocation();

    const navItems = [
        { path: "/dashboard", label: "Library", icon: BookOpen },
        { path: "/collections", label: "Collections", icon: Users },
        // { path: "/reading-lists", label: "Reading Lists", icon: BookMarked },
        // { path: "/teams", label: "Teams", icon: UserPlus },
        { path: "/discover", label: "Discover", icon: Compass },
    ];

    const isActive = (path: string) => {
        if (path === "/dashboard") {
            return location.pathname === "/dashboard";
        }
        return location.pathname.startsWith(path);
    };

    return (
        <nav className="hidden md:flex items-center gap-6">
            {navItems.map((item) => {
                const Icon = item.icon;
                const active = isActive(item.path);
                return (
                    <Link
                        key={item.path}
                        to={item.path}
                        className={cn(
                            "text-sm font-medium transition-smooth flex items-center gap-1.5",
                            active
                                ? "text-primary"
                                : "hover:text-primary text-muted-foreground"
                        )}
                    >
                        <Icon className="h-4 w-4" />
                        {item.label}
                    </Link>
                );
            })}
        </nav>
    );
};

export default AppNavigation;

