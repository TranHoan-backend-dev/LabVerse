import { ACCOUNT_SERVICE_PREDICATE, BASE_API_URL } from "@/type/constant";
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

export const ACCOUNT_SERVICE_URL = `${BASE_API_URL}/${ACCOUNT_SERVICE_PREDICATE}` 

/**
 * Register a new user
 */
export const register = async (request: RegisterRequest): Promise<AuthResponse> => {
  try {
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    const contentType = response.headers.get("content-type");
    const isJson = contentType && contentType.includes("application/json");
    
    if (!response.ok) {
      let errorMessage = `Registration failed: ${response.statusText} (${response.status})`;
      
      if (isJson) {
        try {
          const errorData: WrapperApiResponse = await response.json();
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
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    });

    const contentType = response.headers.get("content-type");
    const isJson = contentType && contentType.includes("application/json");
    
    if (!response.ok) {
      let errorMessage = `Login failed: ${response.statusText} (${response.status})`;
      
      if (isJson) {
        try {
          const errorData: WrapperApiResponse = await response.json();
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
      const response = await fetch(`${ACCOUNT_SERVICE_URL}/auth/logout`, {
        method: "POST",
        headers: getAuthHeaders(),
      });
      
      // Logout should succeed even if API call fails
      if (!response.ok) {
        console.warn("Logout API call returned error, but continuing with local logout");
      }
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
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/users/me`, {
      method: "GET",
      headers: getAuthHeaders(),
    });

    const contentType = response.headers.get("content-type");
    const isJson = contentType && contentType.includes("application/json");

    if (!response.ok) {
      if (response.status === 401) {
        // Token expired or invalid
        tokenStorage.removeToken();
        throw new Error("Session expired. Please login again.");
      }
      
      let errorMessage = `Failed to get user: ${response.statusText} (${response.status})`;
      
      if (isJson) {
        try {
          const errorData: WrapperApiResponse = await response.json();
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
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/users/me`, {
      method: "PATCH",
      headers: getAuthHeaders(),
      body: JSON.stringify(request),
    });

    const contentType = response.headers.get("content-type");
    const isJson = contentType && contentType.includes("application/json");

    if (!response.ok) {
      if (response.status === 401) {
        tokenStorage.removeToken();
        throw new Error("Session expired. Please login again.");
      }
      
      let errorMessage = `Failed to update profile: ${response.statusText} (${response.status})`;
      
      if (isJson) {
        try {
          const errorData: WrapperApiResponse = await response.json();
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
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/users/me/password`, {
      method: "PATCH",
      headers: getAuthHeaders(),
      body: JSON.stringify(request),
    });

    const contentType = response.headers.get("content-type");
    const isJson = contentType && contentType.includes("application/json");

    if (!response.ok) {
      if (response.status === 401) {
        tokenStorage.removeToken();
        throw new Error("Session expired. Please login again.");
      }
      
      let errorMessage = `Failed to change password: ${response.statusText} (${response.status})`;
      
      if (isJson) {
        try {
          const errorData: WrapperApiResponse = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch (e) {
          // If JSON parsing fails, use default message
        }
      }
      
      throw new Error(errorMessage);
    }

    if (!isJson) {
      // Some successful responses might not have JSON body
      if (response.status === 200 || response.status === 204) {
        return;
      }
      throw new Error("Invalid response format from server");
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
  try {
    const response = await fetch(`${ACCOUNT_SERVICE_URL}/auth/forgot-password`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ email }),
    });

    const contentType = response.headers.get("content-type");
    const isJson = contentType && contentType.includes("application/json");

    if (!response.ok) {
      let errorMessage = `Failed to send password reset: ${response.statusText} (${response.status})`;
      
      if (isJson) {
        try {
          const errorData: WrapperApiResponse = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch (e) {
          // If JSON parsing fails, use default message
        }
      }
      
      throw new Error(errorMessage);
    }

    if (!isJson) {
      // Some successful responses might not have JSON body
      if (response.status === 200 || response.status === 204) {
        return;
      }
      throw new Error("Invalid response format from server");
    }

    const apiResponse: WrapperApiResponse<void> = await response.json();
    
    if (apiResponse.status !== 200) {
      throw new Error(apiResponse.message || "Failed to send password reset");
    }
  } catch (error: any) {
    if (error.name === 'TypeError' && error.message.includes('fetch')) {
      throw new Error(`Cannot connect to Account Service at ${ACCOUNT_SERVICE_URL}. Please make sure the service is running.`);
    }
    throw error;
  }
};

