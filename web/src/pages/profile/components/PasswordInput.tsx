// src/pages/profile/components/PasswordInput.tsx
import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Eye, EyeOff } from "lucide-react";

interface PasswordInputProps {
    label: string;
    value: string;
    onChange: (value: string) => void;
    placeholder: string;
    minLength?: number;
    helperText?: string;
}

const PasswordInput = ({
    label,
    value,
    onChange,
    placeholder,
    minLength,
    helperText,
}: PasswordInputProps) => {
    const [show, setShow] = useState(false);

    return (
        <div className="space-y-2">
            <Label>{label}</Label>
            <div className="relative">
                <Input
                    type={show ? "text" : "password"}
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    placeholder={placeholder}
                    minLength={minLength}
                />
                <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                    onClick={() => setShow(!show)}
                >
                    {show ? <EyeOff className="h-4 w-4 text-muted-foreground" /> : <Eye className="h-4 w-4 text-muted-foreground" />}
                </Button>
            </div>
            {helperText && <p className="text-xs text-muted-foreground">{helperText}</p>}
        </div>
    );
};

export default PasswordInput;