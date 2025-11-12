// src/pages/profile/components/AvatarPreview.tsx
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { User } from "lucide-react";

interface AvatarPreviewProps {
    src?: string;
    alt?: string;
    size?: "sm" | "md" | "lg" | "xl";
    fallback?: string;
}

const sizeClasses = {
    sm: "h-12 w-12",
    md: "h-16 w-16",
    lg: "h-24 w-24",
    xl: "h-32 w-32",
};

const AvatarPreview = ({ src, alt, size = "md", fallback = "?" }: AvatarPreviewProps) => {
    return (
        <div className="relative group">
            <Avatar className={`${sizeClasses[size]} ring-4 ring-background shadow-lg`}>
                <AvatarImage src={src} alt={alt} className="object-cover" />
                <AvatarFallback className="bg-primary/10 text-primary text-xl font-semibold">
                    {fallback || <User className="h-6 w-6" />}
                </AvatarFallback>
            </Avatar>

            {/* Hiệu ứng khi có ảnh */}
            {src && (
                <div className="absolute inset-0 rounded-full ring-4 ring-primary/20 opacity-0 group-hover:opacity-100 transition-opacity" />
            )}
        </div>
    );
};

export default AvatarPreview;