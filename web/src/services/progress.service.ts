import {BASE_API_URL,READING_SERVICE_PREDICATE, METHOD} from "@/type/constant.ts";
import {getAuthHeaders} from "@/utils/token";

// Reading service endpoint

// Types
export interface StatusCountResponse {
    status: string;
    count: number;
    percentage: number;
}

export interface TeamMemberProgressResponse {
    userId: string; // Encoded
    totalPapers: number;
    unreadCount: number;
    readingCount: number;
    finishedCount: number;
    averageProgress: number;
    totalProgress: number;
}

export interface CollectionProgressStatisticsResponse {
    collectionId: string; // Encoded
    totalPapers: number;
    totalUsers: number;
    unreadCount: number;
    readingCount: number;
    finishedCount: number;
    averageProgress: number;
    statusDistribution: StatusCountResponse[];
    teamMemberProgress: TeamMemberProgressResponse[];
}

interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
    timestamp?: string;
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

/**
 * Get aggregated progress statistics for a collection
 * This is the main dashboard endpoint for PIs to view team progress
 */
export const getCollectionProgress = async (
    collectionId: string
): Promise<CollectionProgressStatisticsResponse> => {
    const response = await fetch(
        `${BASE_API_URL}/${READING_SERVICE_PREDICATE}/progress/collection/${collectionId}`,
        {
            method: METHOD.GET.toString(),
            headers: getAuthHeaders()
        }
    );
    return handleResponse<CollectionProgressStatisticsResponse>(response);
};

/**
 * Get progress statistics for a specific team member in a collection
 */
export const getTeamMemberProgress = async (
    collectionId: string,
    userId: string
): Promise<TeamMemberProgressResponse> => {
    const response = await fetch(
        `${BASE_API_URL}/${READING_SERVICE_PREDICATE}/progress/collection/${collectionId}/member/${userId}`,
        {
            method: METHOD.GET.toString(),
            headers: getAuthHeaders()
        }
    );
    return handleResponse<TeamMemberProgressResponse>(response);
};

/**
 * Update reading workflow progress
 */
export interface UpdateReadingProgressRequest {
    collectionId: string; // Encoded
    paperId: string; // Encoded
    usersid: string; // Encoded
    lastPage: number;
    progress: number; // 0-100
}

export const updateReadingProgress = async (
    request: UpdateReadingProgressRequest
): Promise<string> => {
    const response = await fetch(
        `${BASE_API_URL}/${READING_SERVICE_PREDICATE}/workflows/progress`,
        {
            method: METHOD.PUT.toString(),
            headers: {
                ...getAuthHeaders(),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(request)
        }
    );
    return handleResponse<string>(response);
};

/**
 * Get workflows by user
 */
export interface ReadingWorkflowResponse {
    collectionId: string; // Encoded
    paperId: string; // Encoded
    usersid: string; // Encoded
    status: string; // "unread" | "reading" | "finished"
    lastPage: number;
    progress: number; // 0-100
}

export const getWorkflowsByUser = async (
    userId: string, // Encoded
    status?: string
): Promise<ReadingWorkflowResponse[]> => {
    const url = status 
        ? `${BASE_API_URL}/${READING_SERVICE_PREDICATE}/workflows/user/${userId}?status=${status}`
        : `${BASE_API_URL}/${READING_SERVICE_PREDICATE}/workflows/user/${userId}`;
    const response = await fetch(url, {
        method: METHOD.GET.toString(),
        headers: getAuthHeaders()
    });
    return handleResponse<ReadingWorkflowResponse[]>(response);
};

/**
 * Create reading workflow
 */
export interface CreateReadingWorkflowRequest {
    collectionId: string; // Encoded
    paperId: string; // Encoded
    usersid: string; // Encoded
}

export const createReadingWorkflow = async (
    request: CreateReadingWorkflowRequest
): Promise<ReadingWorkflowResponse> => {
    const response = await fetch(
        `${BASE_API_URL}/${READING_SERVICE_PREDICATE}/workflows`,
        {
            method: METHOD.POST.toString(),
            headers: {
                ...getAuthHeaders(),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(request)
        }
    );
    return handleResponse<ReadingWorkflowResponse>(response);
};

