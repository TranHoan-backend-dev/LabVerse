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

