import { BASE_API_URL, GROUP_SERVICE_PREDICATE, METHOD } from "@/type/constant.ts";
import { AddTeamMemberRequest, CreateTeamRequest, TeamMemberResponse, TeamResponse, TeamsPageResponse, UpdateMemberRoleRequest, UpdateTeamRequest } from "@/types/team.types";
import { tokenStorage } from "@/utils/token";

const endpoints = ["teams"] as const;

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

// Get teams with pagination and filters
export const getTeams = async (
    search?: string,
    researchField?: string,
    privacy?: 'PUBLIC' | 'PRIVATE',
    page: number = 0,
    size: number = 10
): Promise<TeamsPageResponse> => {
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (researchField) params.append('researchField', researchField);
    if (privacy) params.append('privacy', privacy);
    params.append('page', page.toString());
    params.append('size', size.toString());

    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}?${params.toString()}`, {
        method: METHOD.GET.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    return handleResponse<TeamsPageResponse>(response);
}

// Get team by ID
export const getTeamById = async (teamId: string): Promise<TeamResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${teamId}`, {
        method: METHOD.GET.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    return handleResponse<TeamResponse>(response);
}

// Create team
export const createTeam = async (request: CreateTeamRequest): Promise<TeamResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify(request)
    });
    return handleResponse<TeamResponse>(response);
}

// Update team
export const updateTeam = async (teamId: string, request: UpdateTeamRequest): Promise<TeamResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${teamId}`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify(request)
    });
    return handleResponse<TeamResponse>(response);
}

// Delete team
export const deleteTeam = async (teamId: string): Promise<void> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${teamId}`, {
        method: METHOD.DELETE.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    await handleResponse<void>(response);
}

// Get team members
export const getTeamMembers = async (teamId: string): Promise<TeamMemberResponse[]> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${teamId}/members`, {
        method: METHOD.GET.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    return handleResponse<TeamMemberResponse[]>(response);
}

// Add team member
export const addTeamMember = async (teamId: string, request: AddTeamMemberRequest): Promise<TeamMemberResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${teamId}/members`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify(request)
    });
    return handleResponse<TeamMemberResponse>(response);
}

// Remove team member
export const removeTeamMember = async (teamId: string, memberId: string): Promise<void> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${teamId}/members/${memberId}`, {
        method: METHOD.DELETE.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    await handleResponse<void>(response);
}

// Update member role
export const updateMemberRole = async (
    teamId: string,
    memberId: string,
    request: UpdateMemberRoleRequest
): Promise<TeamMemberResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${teamId}/members/${memberId}/role`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify(request)
    });
    return handleResponse<TeamMemberResponse>(response);
}

