import { useState, useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { forgotPassword as forgotPasswordService } from "@/services/account.service";
import { toast } from "sonner";
import { Mail } from "lucide-react";

interface ForgotPasswordDialogProps {
  open: boolean;
  initialEmail?: string;
  onClose: () => void;
}

export const ForgotPasswordDialog = ({
  open,
  initialEmail = "",
  onClose,
}: ForgotPasswordDialogProps) => {
  const [email, setEmail] = useState(initialEmail);
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);

  // Update email when initialEmail changes (when dialog opens with new email)
  useEffect(() => {
    if (open && initialEmail) {
      setEmail(initialEmail);
    }
  }, [open, initialEmail]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!email) {
      toast.error("Please enter your email address");
      return;
    }

    setIsLoading(true);
    try {
      await forgotPasswordService(email);
      setIsSuccess(true);
      toast.success("Password reset email sent! Please check your inbox.");
    } catch (error: any) {
      toast.error(error.message || "Failed to send password reset email");
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setEmail("");
    setIsSuccess(false);
    onClose();
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Forgot Password?</DialogTitle>
          <DialogDescription>
            {isSuccess
              ? "We've sent a new password to your email address. Please check your inbox and use the new password to sign in."
              : "Enter your email address and we'll send you a new password."}
          </DialogDescription>
        </DialogHeader>
        {isSuccess ? (
          <div className="space-y-4 py-4">
            <div className="flex items-center justify-center p-6 bg-muted rounded-lg">
              <Mail className="h-12 w-12 text-primary" />
            </div>
            <Button onClick={handleClose} className="w-full">
              Close
            </Button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="forgot-email">Email</Label>
              <Input
                id="forgot-email"
                type="email"
                placeholder="name@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                disabled={isLoading}
              />
            </div>
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? "Sending..." : "Send Reset Link"}
            </Button>
            <Button
              type="button"
              variant="ghost"
              onClick={handleClose}
              className="w-full"
              disabled={isLoading}
            >
              Cancel
            </Button>
          </form>
        )}
      </DialogContent>
    </Dialog>
  );
};

