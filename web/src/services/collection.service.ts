import {BASE_API_URL, GROUP_SERVICE_PREDICATE, METHOD} from "@/type/constant.ts";

const endpoints = ["collections", "collections/members"] as const;

// Types
export interface CollectionResponse {
    id: string;
    name: string;
    paperCount?: number;
    memberCount?: number;
    creatorName?: string;
    creatorAvatarUrl?: string;
    currentUserAccessLevel?: 'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR';
}

export interface CollectionPaperDetailResponse {
    paperId: string;
    title: string;
    authors: string;
    journal: string;
    publicationYear?: number;
    priority?: string;
    status?: string;
    addingDate?: string;
}

export interface CollectionPaperRequest {
    collectionId: string;
    paperId: string;
    userId?: string;
    priority?: string;
    status?: string;
}

export interface CollectionRequest {
    name: string;
    userId: string;
}

export interface UpdateCollectionRequest {
    name: string;
    userId: string;
}

export interface CollectionUserRequest {
    collectionId: string;
    memberId: string;
    isAuthor?: boolean;
    accessLevel?: 'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR';
}

export interface CollectionUserResponse {
    memberId: string;
    memberName: string;
    memberEmail: string;
    memberAvatarUrl?: string;
    role: string;
    accessLevel: 'READ_ONLY' | 'CONTRIBUTOR' | 'AUTHOR';
}

export interface CollectionsPageResponse {
    content: CollectionResponse[];
    totalElements: number;
    totalPages: number;
    currentPage: number;
    size: number;
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

// <editor-fold> desc="collections"
export const createCollection = async (name: string, userId: string): Promise<CollectionResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name, userId })
    });
    return handleResponse<CollectionResponse>(response);
}

export const getCollectionById = async (id: string): Promise<CollectionResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${id}`, {
        method: METHOD.GET.toString(),
    });
    return handleResponse<CollectionResponse>(response);
}

export const getPaginatedCollections = async (currentPage: number, pageSize: number = 10): Promise<CollectionsPageResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}?page=${currentPage}&size=${pageSize}`, {
        method: METHOD.GET.toString(),
    });
    return handleResponse<CollectionsPageResponse>(response);
}

export const getMyCollections = async (userId: string): Promise<CollectionsPageResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/my?userId=${encodeURIComponent(userId)}`, {
        method: METHOD.GET.toString(),
    });
    return handleResponse<CollectionsPageResponse>(response);
}

export const getSharedCollections = async (userId: string): Promise<CollectionsPageResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/shared?userId=${encodeURIComponent(userId)}`, {
        method: METHOD.GET.toString(),
    });
    return handleResponse<CollectionsPageResponse>(response);
}

export const updateCollection = async (id: string, name: string, userId: string): Promise<CollectionResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${id}`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name, userId })
    });
    return handleResponse<CollectionResponse>(response);
}

export const deleteCollection = async (id: string, userId: string): Promise<void> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${id}?userId=${encodeURIComponent(userId)}`, {
        method: METHOD.DELETE.toString(),
    });
    await handleResponse<void>(response);
}

export const addPaperToCollection = async (request: CollectionPaperRequest): Promise<CollectionPaperDetailResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/papers`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
    });
    return handleResponse<CollectionPaperDetailResponse>(response);
}

export const updatePaperStatus = async (request: CollectionPaperRequest): Promise<CollectionPaperDetailResponse> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/papers/status`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
    });
    return handleResponse<CollectionPaperDetailResponse>(response);
}

export const removePaperFromCollection = async (collectionId: string, paperId: string, userId: string): Promise<void> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${collectionId}/papers/${paperId}?userId=${encodeURIComponent(userId)}`, {
        method: METHOD.DELETE.toString(),
    });
    await handleResponse<void>(response);
}

export const getPapersInCollection = async (id: string): Promise<CollectionPaperDetailResponse[]> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${id}/papers`, {
        method: METHOD.GET.toString()
    });
    return handleResponse<CollectionPaperDetailResponse[]>(response);
}
// </editor-fold>

// <editor-fold> desc="Collection Members"
export const addMemberToCollection = async (request: CollectionUserRequest): Promise<void> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
    });
    await handleResponse<void>(response);
}

export const removeMemberFromCollection = async (collectionId: string, memberId: string): Promise<void> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}/${collectionId}/${memberId}`, {
        method: METHOD.DELETE.toString(),
    });
    await handleResponse<void>(response);
}

export const getCollectionMembers = async (collectionId: string): Promise<CollectionUserResponse[]> => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}/${collectionId}`, {
        method: METHOD.GET.toString()
    });
    return handleResponse<CollectionUserResponse[]>(response);
}
// </editor-fold>