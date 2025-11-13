import { useState, useEffect } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { InputOTP, InputOTPGroup, InputOTPSlot } from "@/components/ui/input-otp";
import { useAuth } from "@/contexts/AuthContext";
import { toast } from "sonner";

interface OtpVerificationDialogProps {
  open: boolean;
  email: string;
  onVerified?: () => void;
  onClose?: () => void;
}

export const OtpVerificationDialog = ({
  open,
  email,
  onVerified,
  onClose,
}: OtpVerificationDialogProps) => {
  const [otp, setOtp] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [timeLeft, setTimeLeft] = useState(600); // 10 minutes in seconds
  const { verifyOtp } = useAuth();

  useEffect(() => {
    if (open) {
      setOtp("");
      setTimeLeft(600);
    }
  }, [open]);

  useEffect(() => {
    if (!open || timeLeft <= 0) return;

    const timer = setInterval(() => {
      setTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [open, timeLeft]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, "0")}`;
  };

  const handleVerify = async () => {
    if (otp.length !== 6) {
      toast.error("Please enter a complete 6-digit code");
      return;
    }

    setIsLoading(true);
    try {
      await verifyOtp(email, otp);
      toast.success("Email verified successfully!");
      onVerified?.();
    } catch (error: any) {
      toast.error(error.message || "Failed to verify OTP");
      setOtp("");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(open) => !open && onClose?.()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Verify Your Email</DialogTitle>
          <DialogDescription>
            We've sent a 6-digit verification code to <strong>{email}</strong>
            <br />
            Please enter the code below to complete your registration.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4 py-4">
          <div className="flex justify-center">
            <InputOTP
              maxLength={6}
              value={otp}
              onChange={setOtp}
              onComplete={handleVerify}
            >
              <InputOTPGroup>
                <InputOTPSlot index={0} />
                <InputOTPSlot index={1} />
                <InputOTPSlot index={2} />
                <InputOTPSlot index={3} />
                <InputOTPSlot index={4} />
                <InputOTPSlot index={5} />
              </InputOTPGroup>
            </InputOTP>
          </div>
          <div className="text-center text-sm text-muted-foreground">
            {timeLeft > 0 ? (
              <span>Code expires in: <strong>{formatTime(timeLeft)}</strong></span>
            ) : (
              <span className="text-destructive">Code has expired. Please register again.</span>
            )}
          </div>
          <Button
            onClick={handleVerify}
            disabled={otp.length !== 6 || isLoading || timeLeft <= 0}
            className="w-full"
          >
            {isLoading ? "Verifying..." : "Verify Email"}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
};

