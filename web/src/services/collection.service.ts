import {BASE_API_URL, GROUP_SERVICE_PREDICATE, METHOD} from "@/type/constant.ts";
import {IdEncoder} from "@/utils/idEncoder.ts";

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
    // Encode userId before sending to backend
    const encodedUserId = IdEncoder.encode(userId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name, userId: encodedUserId })
    });
    return handleResponse<CollectionResponse>(response);
}

export const getCollectionById = async (id: string): Promise<CollectionResponse> => {
    // Collection ID from response is already encoded, so use it directly
    // Only encode if it's a raw UUID (not already encoded)
    const encodedId = id.includes('-') ? IdEncoder.encode(id) : id;
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}`, {
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
    // Encode userId before sending to backend
    const encodedUserId = IdEncoder.encode(userId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/my?userId=${encodeURIComponent(encodedUserId)}`, {
        method: METHOD.GET.toString(),
    });
    return handleResponse<CollectionsPageResponse>(response);
}

export const getSharedCollections = async (userId: string): Promise<CollectionsPageResponse> => {
    // Encode userId before sending to backend
    const encodedUserId = IdEncoder.encode(userId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/shared?userId=${encodeURIComponent(encodedUserId)}`, {
        method: METHOD.GET.toString(),
    });
    return handleResponse<CollectionsPageResponse>(response);
}

export const updateCollection = async (id: string, name: string, userId: string): Promise<CollectionResponse> => {
    // Collection ID from response is already encoded, so use it directly
    // Only encode if it's a raw UUID (not already encoded)
    const encodedId = id.includes('-') ? IdEncoder.encode(id) : id;
    // Encode userId before sending to backend (userId is always raw UUID)
    const encodedUserId = IdEncoder.encode(userId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ name, userId: encodedUserId })
    });
    return handleResponse<CollectionResponse>(response);
}

export const deleteCollection = async (id: string, userId: string): Promise<void> => {
    // Collection ID from response is already encoded, so use it directly
    // Only encode if it's a raw UUID (not already encoded)
    const encodedId = id.includes('-') ? IdEncoder.encode(id) : id;
    // Encode userId before sending to backend (userId is always raw UUID)
    const encodedUserId = IdEncoder.encode(userId);
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}?userId=${encodeURIComponent(encodedUserId)}`, {
        method: METHOD.DELETE.toString(),
    });
    await handleResponse<void>(response);
}

export const addPaperToCollection = async (request: CollectionPaperRequest): Promise<CollectionPaperDetailResponse> => {
    // Encode IDs before sending to backend
    // Collection ID from response is already encoded
    // Paper ID from getAllPapers is already encoded by paper service, so use it directly
    // userId is raw UUID, needs encoding
    const encodedRequest = {
        ...request,
        collectionId: request.collectionId.includes('-') ? IdEncoder.encode(request.collectionId) : request.collectionId,
        // Paper ID from paper service getAllPapers is already encoded, so use it directly
        // Only encode if it looks like a raw UUID (contains dashes)
        paperId: request.paperId.includes('-') ? IdEncoder.encode(request.paperId) : request.paperId,
        userId: request.userId ? IdEncoder.encode(request.userId) : undefined, // userId is always raw UUID
    };
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/papers`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(encodedRequest)
    });
    return handleResponse<CollectionPaperDetailResponse>(response);
}

export const updatePaperStatus = async (request: CollectionPaperRequest): Promise<CollectionPaperDetailResponse> => {
    // Encode IDs before sending to backend
    // Collection ID from response is already encoded, paperId and userId are raw UUIDs
    const encodedRequest = {
        ...request,
        collectionId: request.collectionId.includes('-') ? IdEncoder.encode(request.collectionId) : request.collectionId,
        paperId: IdEncoder.encode(request.paperId), // paperId is always raw UUID
        userId: request.userId ? IdEncoder.encode(request.userId) : undefined, // userId is always raw UUID
    };
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/papers/status`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(encodedRequest)
    });
    return handleResponse<CollectionPaperDetailResponse>(response);
}

export const removePaperFromCollection = async (collectionId: string, paperId: string, userId: string): Promise<void> => {
    // Encode IDs before sending to backend
    // Collection ID from response is already encoded, paperId and userId are raw UUIDs
    const encodedCollectionId = collectionId.includes('-') ? IdEncoder.encode(collectionId) : collectionId;
    const encodedPaperId = IdEncoder.encode(paperId); // paperId is always raw UUID
    const encodedUserId = IdEncoder.encode(userId); // userId is always raw UUID
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedCollectionId}/papers/${encodedPaperId}?userId=${encodeURIComponent(encodedUserId)}`, {
        method: METHOD.DELETE.toString(),
    });
    await handleResponse<void>(response);
}

export const getPapersInCollection = async (id: string): Promise<CollectionPaperDetailResponse[]> => {
    // Collection ID from response is already encoded, so use it directly
    // Only encode if it's a raw UUID (not already encoded)
    const encodedId = id.includes('-') ? IdEncoder.encode(id) : id;
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${encodedId}/papers`, {
        method: METHOD.GET.toString()
    });
    return handleResponse<CollectionPaperDetailResponse[]>(response);
}
// </editor-fold>

// <editor-fold> desc="Collection Members"
export const addMemberToCollection = async (request: CollectionUserRequest): Promise<void> => {
    // Encode IDs before sending to backend
    // Collection ID from response is already encoded, memberId is raw UUID
    const encodedRequest = {
        ...request,
        collectionId: request.collectionId.includes('-') ? IdEncoder.encode(request.collectionId) : request.collectionId,
        memberId: IdEncoder.encode(request.memberId), // memberId is always raw UUID
    };
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(encodedRequest)
    });
    await handleResponse<void>(response);
}

export const removeMemberFromCollection = async (collectionId: string, memberId: string): Promise<void> => {
    // Encode IDs before sending to backend
    // Collection ID from response is already encoded, memberId is raw UUID
    const encodedCollectionId = collectionId.includes('-') ? IdEncoder.encode(collectionId) : collectionId;
    const encodedMemberId = IdEncoder.encode(memberId); // memberId is always raw UUID
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}/${encodedCollectionId}/${encodedMemberId}`, {
        method: METHOD.DELETE.toString(),
    });
    await handleResponse<void>(response);
}

export const getCollectionMembers = async (collectionId: string): Promise<CollectionUserResponse[]> => {
    // Collection ID from response is already encoded, so use it directly
    // Only encode if it's a raw UUID (not already encoded)
    const encodedCollectionId = collectionId.includes('-') ? IdEncoder.encode(collectionId) : collectionId;
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}/${encodedCollectionId}`, {
        method: METHOD.GET.toString()
    });
    return handleResponse<CollectionUserResponse[]>(response);
}
// </editor-fold>