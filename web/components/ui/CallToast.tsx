import { addToast } from "@heroui/toast";
import { CircularProgress } from "@heroui/progress";

interface ToastProps {
  message: string;
  title?: string;
  color:
    | "success"
    | "danger"
    | "warning"
    | "default"
    | "foreground"
    | "primary"
    | "secondary";
  isCircularProgress?: true;
}

const duration = 3000;

export const callToast = ({
  message,
  title,
  color,
  isCircularProgress,
}: ToastProps) => {
  addToast({
    ...(title && { title }),
    description: (
      <div className="flex items-center gap-2">
        {isCircularProgress && (
          <CircularProgress aria-label="Loading..." size="sm" />
        )}
        <span>{message}</span>
      </div>
    ),
    color: color,
    closeIcon: "true",
    shouldShowTimeoutProgress: true,
    timeout: duration,
  });
};
