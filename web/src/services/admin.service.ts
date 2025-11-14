import { BASE_API_URL, ACCOUNT_SERVICE_PREDICATE, METHOD } from "@/type/constant";
import { getAuthHeaders } from "@/utils/token";

const ADMIN_SERVICE_BASE_URL = `${BASE_API_URL}/${ACCOUNT_SERVICE_PREDICATE}/admin`;

export interface AdminUser {
    id: string;
    email: string;
    username: string;
    fullName: string;
    avatarUrl: string | null;
    role: string;
    createdDate: string;
    updatedDate: string;
}

export interface AdminUserDetails extends AdminUser {
    isActive: boolean;
    paperCount: number;
    teamCount: number;
    collectionCount: number;
}

export interface AdminCollection {
    id: string;
    name: string;
    paperCount: number;
    memberCount: number;
    creatorName?: string;
    creatorEmail?: string;
}

export interface AdminStatistics {
    totalUsers: number;
    activeUsers: number;
    inactiveUsers: number;
    totalPapers: number;
    papersThisMonth: number;
    totalCollections: number;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    size: number;
}

// User Management APIs
export const getAdminUsers = async (
    page: number = 0,
    size: number = 20,
    search?: string,
    role?: string,
    isActive?: boolean
): Promise<PaginatedResponse<AdminUser>> => {
    try {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
        });
        if (search) params.append("search", search);
        if (role) params.append("role", role);
        if (isActive !== undefined) params.append("isActive", isActive.toString());

        const response = await fetch(
            `${ADMIN_SERVICE_BASE_URL}/users?${params}`,
            {
                method: METHOD.GET,
                headers: getAuthHeaders(),
            }
        );

        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.includes("application/json");

        if (!response.ok) {
            let errorMessage = `Failed to fetch users: ${response.statusText} (${response.status})`;
            
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

        const apiResponse = await response.json();
        
        if (apiResponse.status !== 200 || !apiResponse.data) {
            throw new Error(apiResponse.message || "Failed to fetch users");
        }

        return apiResponse.data;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Admin Service at ${ADMIN_SERVICE_BASE_URL}. Please make sure the service is running.`);
        }
        throw error;
    }
};

export const getUserDetails = async (userId: string): Promise<AdminUserDetails> => {
    try {
        const response = await fetch(
            `${ADMIN_SERVICE_BASE_URL}/users/${userId}`,
            {
                method: METHOD.GET,
                headers: getAuthHeaders(),
            }
        );

        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.includes("application/json");

        if (!response.ok) {
            let errorMessage = `Failed to fetch user details: ${response.statusText} (${response.status})`;
            
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

        const apiResponse = await response.json();
        
        if (apiResponse.status !== 200 || !apiResponse.data) {
            throw new Error(apiResponse.message || "Failed to fetch user details");
        }

        return apiResponse.data;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Admin Service at ${ADMIN_SERVICE_BASE_URL}. Please make sure the service is running.`);
        }
        throw error;
    }
};

export const activateUser = async (userId: string): Promise<void> => {
    try {
        const response = await fetch(
            `${ADMIN_SERVICE_BASE_URL}/users/${userId}/activate`,
            {
                method: METHOD.PATCH,
                headers: getAuthHeaders(),
            }
        );

        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.includes("application/json");

        if (!response.ok) {
            let errorMessage = `Failed to activate user: ${response.statusText} (${response.status})`;
            
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
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Admin Service at ${ADMIN_SERVICE_BASE_URL}. Please make sure the service is running.`);
        }
        throw error;
    }
};

export const deactivateUser = async (userId: string): Promise<void> => {
    try {
        const response = await fetch(
            `${ADMIN_SERVICE_BASE_URL}/users/${userId}/deactivate`,
            {
                method: METHOD.PATCH,
                headers: getAuthHeaders(),
            }
        );

        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.includes("application/json");

        if (!response.ok) {
            let errorMessage = `Failed to deactivate user: ${response.statusText} (${response.status})`;
            
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
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Admin Service at ${ADMIN_SERVICE_BASE_URL}. Please make sure the service is running.`);
        }
        throw error;
    }
};

export const changeUserRole = async (userId: string, roleId: string): Promise<AdminUser> => {
    try {
        const response = await fetch(
            `${ADMIN_SERVICE_BASE_URL}/users/${userId}/role`,
            {
                method: METHOD.PATCH,
                headers: {
                    ...getAuthHeaders(),
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({ roleId }),
            }
        );

        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.includes("application/json");

        if (!response.ok) {
            let errorMessage = `Failed to change user role: ${response.statusText} (${response.status})`;
            
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

        const apiResponse = await response.json();
        
        if (apiResponse.status !== 200 || !apiResponse.data) {
            throw new Error(apiResponse.message || "Failed to change user role");
        }

        return apiResponse.data;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Admin Service at ${ADMIN_SERVICE_BASE_URL}. Please make sure the service is running.`);
        }
        throw error;
    }
};

export const deleteUser = async (userId: string): Promise<void> => {
    try {
        const response = await fetch(
            `${ADMIN_SERVICE_BASE_URL}/users/${userId}`,
            {
                method: METHOD.DELETE,
                headers: getAuthHeaders(),
            }
        );

        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.includes("application/json");

        if (!response.ok) {
            let errorMessage = `Failed to delete user: ${response.statusText} (${response.status})`;
            
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
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Admin Service at ${ADMIN_SERVICE_BASE_URL}. Please make sure the service is running.`);
        }
        throw error;
    }
};

// Collection Management APIs
export const getAdminCollections = async (
    page: number = 0,
    size: number = 20,
    search?: string
): Promise<PaginatedResponse<AdminCollection>> => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });
    if (search) params.append("search", search);

    const response = await fetch(
        `${BASE_API_URL}/group-service/collections?${params}`,
        {
            method: METHOD.GET,
            headers: getAuthHeaders(),
        }
    );

    if (!response.ok) {
        throw new Error(`Failed to fetch collections: ${response.statusText}`);
    }

    const data = await response.json();
    // Transform to match AdminCollection interface
    const collections = (data.data?.content || []).map((c: any) => ({
        id: c.id,
        name: c.name,
        paperCount: c.paperCount || 0,
        memberCount: c.memberCount || 0,
        creatorName: c.creatorName,
        creatorEmail: c.creatorEmail,
    }));

    return {
        content: collections,
        totalElements: data.data?.totalElements || 0,
        totalPages: data.data?.totalPages || 0,
        currentPage: data.data?.currentPage || page,
        size: data.data?.size || size,
    };
};

export const deleteCollection = async (collectionId: string, userId: string): Promise<void> => {
    const response = await fetch(
        `${BASE_API_URL}/group-service/collections/${collectionId}?userId=${encodeURIComponent(userId)}`,
        {
            method: METHOD.DELETE,
            headers: getAuthHeaders(),
        }
    );

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || "Failed to delete collection");
    }
};

// Statistics APIs
export const getAdminStatistics = async (): Promise<AdminStatistics> => {
    try {
        const response = await fetch(
            `${ADMIN_SERVICE_BASE_URL}/statistics/overview`,
            {
                method: METHOD.GET,
                headers: getAuthHeaders(),
            }
        );

        const contentType = response.headers.get("content-type");
        const isJson = contentType && contentType.includes("application/json");

        if (!response.ok) {
            let errorMessage = `Failed to fetch statistics: ${response.statusText} (${response.status})`;
            
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

        const apiResponse = await response.json();
        
        if (apiResponse.status !== 200 || !apiResponse.data) {
            throw new Error(apiResponse.message || "Failed to fetch statistics");
        }

        return apiResponse.data;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Admin Service at ${ADMIN_SERVICE_BASE_URL}. Please make sure the service is running.`);
        }
        throw error;
    }
};

