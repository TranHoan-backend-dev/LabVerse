import { ACCOUNT_SERVICE_URL } from "@/type/constant.ts";
import type {
  RegisterRequest,
  LoginRequest,
  AuthResponse,
  User,
  WrapperApiResponse,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from "@/types/auth.types";
import { getAuthHeaders, tokenStorage } from "@/utils/token";

/**
 * Register a new user
 */
export const register = async (request: RegisterRequest): Promise<AuthResponse> => {
  try {
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/api/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: "Registration failed" }));
      throw new Error(errorData.message || `Registration failed: ${response.statusText} (${response.status})`);
    }

    const apiResponse: WrapperApiResponse<AuthResponse> = await response.json();
    
    if (apiResponse.status !== 200 || !apiResponse.data) {
      throw new Error(apiResponse.message || "Registration failed");
    }

    // Store token and user data
    tokenStorage.setToken(apiResponse.data.token);
    tokenStorage.setUser({
      id: apiResponse.data.userId,
      email: apiResponse.data.email,
      username: apiResponse.data.username,
      fullName: apiResponse.data.fullName,
      avatarUrl: apiResponse.data.avatarUrl,
      role: apiResponse.data.role,
    });

    return apiResponse.data;
  } catch (error: any) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error(`Cannot connect to Account Service at ${ACCOUNT_SERVICE_URL}. Please make sure the service is running.`);
    }
    throw error;
  }
};

/**
 * Login with email and password
 */
export const login = async (request: LoginRequest): Promise<AuthResponse> => {
  try {
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/api/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: "Login failed" }));
      throw new Error(errorData.message || `Login failed: ${response.statusText} (${response.status})`);
    }

    const apiResponse: WrapperApiResponse<AuthResponse> = await response.json();
    
    if (apiResponse.status !== 200 || !apiResponse.data) {
      throw new Error(apiResponse.message || "Login failed");
    }

    // Store token and user data
    tokenStorage.setToken(apiResponse.data.token);
    tokenStorage.setUser({
      id: apiResponse.data.userId,
      email: apiResponse.data.email,
      username: apiResponse.data.username,
      fullName: apiResponse.data.fullName,
      avatarUrl: apiResponse.data.avatarUrl,
      role: apiResponse.data.role,
    });

    return apiResponse.data;
  } catch (error: any) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error(`Cannot connect to Account Service at ${ACCOUNT_SERVICE_URL}. Please make sure the service is running.`);
    }
    throw error;
  }
};

/**
 * Logout - clears token and user data
 */
export const logout = async (): Promise<void> => {
  const token = tokenStorage.getToken();
  
  if (token) {
    try {
      await fetch(`${ACCOUNT_SERVICE_URL}/api/auth/logout`, {
        method: "POST",
        headers: getAuthHeaders(),
      });
    } catch (error) {
      console.error("Logout API call failed:", error);
      // Continue with local logout even if API call fails
    }
  }

  // Clear local storage
  tokenStorage.removeToken();
};

/**
 * Get current user profile
 */
export const getCurrentUser = async (): Promise<User> => {
  try {
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/api/users/me`, {
      method: "GET",
      headers: getAuthHeaders(),
    });

    if (!response.ok) {
      if (response.status === 401) {
        // Token expired or invalid
        tokenStorage.removeToken();
        throw new Error("Session expired. Please login again.");
      }
      const errorData = await response.json().catch(() => ({ message: "Failed to get user" }));
      throw new Error(errorData.message || `Failed to get user: ${response.statusText} (${response.status})`);
    }

    const apiResponse: WrapperApiResponse<User> = await response.json();
    
    if (apiResponse.status !== 200 || !apiResponse.data) {
      throw new Error(apiResponse.message || "Failed to get user");
    }

    // Update stored user data
    tokenStorage.setUser(apiResponse.data);

    return apiResponse.data;
  } catch (error: any) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error(`Cannot connect to Account Service at ${ACCOUNT_SERVICE_URL}. Please make sure the service is running.`);
    }
    throw error;
  }
};

/**
 * Update user profile
 */
export const updateProfile = async (request: UpdateProfileRequest): Promise<User> => {
  try {
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/api/users/me`, {
      method: "PATCH",
      headers: getAuthHeaders(),
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      if (response.status === 401) {
        tokenStorage.removeToken();
        throw new Error("Session expired. Please login again.");
      }
      const errorData = await response.json().catch(() => ({ message: "Failed to update profile" }));
      throw new Error(errorData.message || `Failed to update profile: ${response.statusText} (${response.status})`);
    }

    const apiResponse: WrapperApiResponse<User> = await response.json();
    
    if (apiResponse.status !== 200 || !apiResponse.data) {
      throw new Error(apiResponse.message || "Failed to update profile");
    }

    // Update stored user data
    tokenStorage.setUser(apiResponse.data);

    return apiResponse.data;
  } catch (error: any) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error(`Cannot connect to Account Service at ${ACCOUNT_SERVICE_URL}. Please make sure the service is running.`);
    }
    throw error;
  }
};

/**
 * Change password
 */
export const changePassword = async (request: ChangePasswordRequest): Promise<void> => {
  try {
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/api/users/me/password`, {
      method: "PATCH",
      headers: getAuthHeaders(),
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      if (response.status === 401) {
        tokenStorage.removeToken();
        throw new Error("Session expired. Please login again.");
      }
      const errorData = await response.json().catch(() => ({ message: "Failed to change password" }));
      throw new Error(errorData.message || `Failed to change password: ${response.statusText} (${response.status})`);
    }

    const apiResponse: WrapperApiResponse<void> = await response.json();
    
    if (apiResponse.status !== 200) {
      throw new Error(apiResponse.message || "Failed to change password");
    }
  } catch (error: any) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error(`Cannot connect to Account Service at ${ACCOUNT_SERVICE_URL}. Please make sure the service is running.`);
    }
    throw error;
  }
};

/**
 * Forgot password - request password reset
 */
export const forgotPassword = async (email: string): Promise<void> => {
  const response = await fetch(`${ACCOUNT_SERVICE_URL}/api/auth/forgot-password`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: "Failed to send password reset" }));
    throw new Error(errorData.message || `Failed to send password reset: ${response.statusText}`);
  }

  const apiResponse: WrapperApiResponse<void> = await response.json();
  
  if (apiResponse.status !== 200) {
    throw new Error(apiResponse.message || "Failed to send password reset");
  }
};

