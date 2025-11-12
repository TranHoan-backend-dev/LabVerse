import {BASE_API_URL, READING_SERVICE_PREDICATE, METHOD} from "@/type/constant.ts";
import {tokenStorage} from "@/utils/token";

const endpoints = ["reading-lists"] as const;

// Types
export interface ReadingListResponse {
    id: string;
    name: string;
    description?: string;
    paperIds?: string[];
    userIds?: string[];
    createdAt?: string;
    updatedAt?: string;
}

export interface CreateReadingListRequest {
    name: string;
    description?: string;
    userId: string;
}

export interface UpdateReadingListPapersRequest {
    paperIds: string[];
}

export interface UpdateReadingListUsersRequest {
    userIds: string[];
}

interface ApiResponse<T> {
    success: boolean;
    data: T;
    message?: string;
}

// Helper function to handle API responses
async function handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: 'Unknown error' }));
        const errorMessage = error.message || error.data || `HTTP error! status: ${response.status}`;
        throw new Error(errorMessage);
    }
    const result: ApiResponse<T> = await response.json();
    return result.data;
}

// Get auth token from storage
function getAuthToken(): string | null {
    const token = tokenStorage.getToken();
    return token ? `Bearer ${token}` : null;
}

// Create reading list
export const createReadingList = async (request: CreateReadingListRequest): Promise<ReadingListResponse> => {
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({
            name: request.name,
            description: request.description,
            userId: request.userId
        })
    });
    return handleResponse<ReadingListResponse>(response);
}

// Get reading lists by user
export const getReadingListsByUser = async (userId: string): Promise<ReadingListResponse[]> => {
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/user/${userId}`, {
        method: METHOD.GET.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    return handleResponse<ReadingListResponse[]>(response);
}

// Get reading list by ID
export const getReadingListById = async (listId: string): Promise<ReadingListResponse> => {
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/${listId}`, {
        method: METHOD.GET.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    return handleResponse<ReadingListResponse>(response);
}

// Update papers in reading list
export const updateReadingListPapers = async (
    listId: string,
    request: UpdateReadingListPapersRequest
): Promise<ReadingListResponse> => {
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/${listId}/papers`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({ paperIds: request.paperIds })
    });
    return handleResponse<ReadingListResponse>(response);
}

// Update users in reading list
export const updateReadingListUsers = async (
    listId: string,
    request: UpdateReadingListUsersRequest
): Promise<ReadingListResponse> => {
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/${listId}/users`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({ userIds: request.userIds })
    });
    return handleResponse<ReadingListResponse>(response);
}

// Delete reading list
export const deleteReadingList = async (listId: string): Promise<void> => {
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/${listId}`, {
        method: METHOD.DELETE.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    await handleResponse<void>(response);
}

