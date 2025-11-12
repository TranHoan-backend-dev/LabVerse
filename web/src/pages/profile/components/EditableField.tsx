// src/pages/profile/components/EditableField.tsx
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

interface EditableFieldProps {
    label: string;
    value: string;
    onChange: (value: string) => void;
    disabled: boolean;
    placeholder?: string;
    minLength?: number;
    maxLength?: number;
}

const EditableField = ({
    label,
    value,
    onChange,
    disabled,
    placeholder,
    minLength,
    maxLength,
}: EditableFieldProps) => {
    return (
        <div className="space-y-2">
            <Label>{label}</Label>
            <Input
                value={value}
                onChange={(e) => onChange(e.target.value)}
                disabled={disabled}
                placeholder={placeholder}
                minLength={minLength}
                maxLength={maxLength}
            />
        </div>
    );
};

export default EditableField;