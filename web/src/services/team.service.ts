import {BASE_API_URL, GROUP_SERVICE_PREDICATE, METHOD} from "@/type/constant.ts";
import {IdEncoder} from "@/utils/idEncoder.ts";
import {tokenStorage} from "@/utils/token";

const endpoints = ["teams"] as const;

// Types
export interface TeamResponse {
    id: string;
    name: string;
    description?: string;
    researchField?: string;
    privacy: 'PUBLIC' | 'PRIVATE';
    iconUrl?: string;
    memberCount?: number;
    paperCount?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface TeamMemberResponse {
    memberId: string;
    memberName: string;
    memberEmail: string;
    memberAvatarUrl?: string;
    role: 'OWNER' | 'ADMIN' | 'MEMBER';
    joinedAt?: string;
}

export interface TeamsPageResponse {
    content: TeamResponse[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    size: number;
}

export interface CreateTeamRequest {
    name: string;
    description?: string;
    researchField?: string;
    privacy: 'PUBLIC' | 'PRIVATE';
    iconUrl?: string;
    userId: string;
}

export interface UpdateTeamRequest {
    name?: string;
    description?: string;
    researchField?: string;
    privacy?: 'PUBLIC' | 'PRIVATE';
    iconUrl?: string;
    userId: string;
}

export interface AddTeamMemberRequest {
    memberId: string;
    role?: 'ADMIN' | 'MEMBER';
}

export interface UpdateMemberRoleRequest {
    role: 'OWNER' | 'ADMIN' | 'MEMBER';
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
    const encodedId = teamId.includes('-') ? IdEncoder.encode(teamId) : teamId;
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}`, {
        method: METHOD.GET.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    return handleResponse<TeamResponse>(response);
}

// Create team
export const createTeam = async (request: CreateTeamRequest): Promise<TeamResponse> => {
    const encodedUserId = IdEncoder.encode(request.userId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({
            ...request,
            userId: encodedUserId
        })
    });
    return handleResponse<TeamResponse>(response);
}

// Update team
export const updateTeam = async (teamId: string, request: UpdateTeamRequest): Promise<TeamResponse> => {
    const encodedId = teamId.includes('-') ? IdEncoder.encode(teamId) : teamId;
    const encodedUserId = IdEncoder.encode(request.userId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({
            ...request,
            userId: encodedUserId
        })
    });
    return handleResponse<TeamResponse>(response);
}

// Delete team
export const deleteTeam = async (teamId: string): Promise<void> => {
    const encodedId = teamId.includes('-') ? IdEncoder.encode(teamId) : teamId;
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}`, {
        method: METHOD.DELETE.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    await handleResponse<void>(response);
}

// Get team members
export const getTeamMembers = async (teamId: string): Promise<TeamMemberResponse[]> => {
    const encodedId = teamId.includes('-') ? IdEncoder.encode(teamId) : teamId;
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}/members`, {
        method: METHOD.GET.toString(),
        headers: {
            'Authorization': getAuthToken() || ''
        }
    });
    return handleResponse<TeamMemberResponse[]>(response);
}

// Add team member
export const addTeamMember = async (teamId: string, request: AddTeamMemberRequest): Promise<TeamMemberResponse> => {
    const encodedId = teamId.includes('-') ? IdEncoder.encode(teamId) : teamId;
    const encodedMemberId = IdEncoder.encode(request.memberId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}/members`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify({
            ...request,
            memberId: encodedMemberId
        })
    });
    return handleResponse<TeamMemberResponse>(response);
}

// Remove team member
export const removeTeamMember = async (teamId: string, memberId: string): Promise<void> => {
    const encodedId = teamId.includes('-') ? IdEncoder.encode(teamId) : teamId;
    const encodedMemberId = IdEncoder.encode(memberId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}/members/${encodedMemberId}`, {
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
    const encodedId = teamId.includes('-') ? IdEncoder.encode(teamId) : teamId;
    const encodedMemberId = IdEncoder.encode(memberId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}/members/${encodedMemberId}/role`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json',
            'Authorization': getAuthToken() || ''
        },
        body: JSON.stringify(request)
    });
    return handleResponse<TeamMemberResponse>(response);
}

