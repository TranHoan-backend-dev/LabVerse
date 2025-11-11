import { BASE_API_URL } from "@/type/constant.ts";
import { getAuthHeaders } from "@/utils/token";

const NOTIFICATION_SERVICE_URL = `${BASE_API_URL}/notification-service`;

export interface Notification {
  id: string;
  title: string;
  message: string;
  linkTo: string | null;
  isRead: boolean;
  createdAt: string;
}

/**
 * Get all notifications for the current user
 */
export const getNotifications = async (): Promise<Notification[]> => {
  try {
    // Gateway rewrites /notification-service/** to /v1/api/**
    const response = await fetch(`${NOTIFICATION_SERVICE_URL}/notifications`, {
      method: "GET",
      headers: getAuthHeaders(),
    });

    const contentType = response.headers.get("content-type");
    const isJson = contentType && contentType.includes("application/json");

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("Session expired. Please login again.");
      }
      
      let errorMessage = `Failed to get notifications: ${response.statusText} (${response.status})`;
      
      if (isJson) {
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch (e) {
          // If JSON parsing fails, use default message
        }
      }
      
      throw new Error(errorMessage);
    }

    if (!isJson) {
      throw new Error("Invalid response format from server");
    }

    const notifications: Notification[] = await response.json();
    return notifications;
  } catch (error: any) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error(`Cannot connect to Notification Service at ${NOTIFICATION_SERVICE_URL}. Please make sure the service is running.`);
    }
    throw error;
  }
};

/**
 * Mark notification as read
 */
export const markAsRead = async (notificationId: string): Promise<void> => {
  try {
    // Gateway rewrites /notification-service/** to /v1/api/**
    const response = await fetch(`${NOTIFICATION_SERVICE_URL}/notifications/${notificationId}/read`, {
      method: "PUT",
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error("Session expired. Please login again.");
      }
      
      let errorMessage = `Failed to mark notification as read: ${response.statusText} (${response.status})`;
      
      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch (e) {
          // If JSON parsing fails, use default message
        }
      }
      
      throw new Error(errorMessage);
    }
  } catch (error: any) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error(`Cannot connect to Notification Service at ${NOTIFICATION_SERVICE_URL}. Please make sure the service is running.`);
    }
    throw error;
  }
};

