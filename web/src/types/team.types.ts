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