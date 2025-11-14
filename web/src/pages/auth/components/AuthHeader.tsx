import React from 'react';
import {BookOpen} from "lucide-react";
import {Link} from "react-router-dom";

const AuthHeader: React.FC = () => {
    return (
        <Link to="/" className="flex items-center justify-center gap-2 transition-smooth hover:opacity-80">
            <BookOpen className="h-8 w-8 text-primary"/>
            <span className="text-2xl font-bold text-gradient">LabVerse</span>
        </Link>
    );
};

export default AuthHeader;
