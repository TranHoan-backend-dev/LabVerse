import {BASE_API_URL, READING_SERVICE_PREDICATE, METHOD} from "@/type/constant.ts";
import {IdEncoder} from "@/utils/idEncoder.ts";
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
    const encodedUserId = IdEncoder.encode(request.userId);
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({
            name: request.name,
            description: request.description,
            userId: encodedUserId
        })
    });
    return handleResponse<ReadingListResponse>(response);
}

// Get reading lists by user
export const getReadingListsByUser = async (userId: string): Promise<ReadingListResponse[]> => {
    const encodedUserId = IdEncoder.encode(userId);
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/user/${encodedUserId}`, {
        method: METHOD.GET.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    return handleResponse<ReadingListResponse[]>(response);
}

// Get reading list by ID
export const getReadingListById = async (listId: string): Promise<ReadingListResponse> => {
    // List ID from response is already encoded, use directly
    const encodedId = listId.includes('-') ? IdEncoder.encode(listId) : listId;
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}`, {
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
    const encodedId = listId.includes('-') ? IdEncoder.encode(listId) : listId;
    // Paper IDs from paper service are already encoded
    const encodedPaperIds = request.paperIds.map(id => 
        id.includes('-') ? IdEncoder.encode(id) : id
    );
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}/papers`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({ paperIds: encodedPaperIds })
    });
    return handleResponse<ReadingListResponse>(response);
}

// Update users in reading list
export const updateReadingListUsers = async (
    listId: string,
    request: UpdateReadingListUsersRequest
): Promise<ReadingListResponse> => {
    const encodedId = listId.includes('-') ? IdEncoder.encode(listId) : listId;
    const encodedUserIds = request.userIds.map(id => IdEncoder.encode(id));
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}/users`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({ userIds: encodedUserIds })
    });
    return handleResponse<ReadingListResponse>(response);
}

// Delete reading list
export const deleteReadingList = async (listId: string): Promise<void> => {
    const encodedId = listId.includes('-') ? IdEncoder.encode(listId) : listId;
    const response = await fetch(`${BASE_API_URL}/${READING_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}`, {
        method: METHOD.DELETE.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    await handleResponse<void>(response);
}

