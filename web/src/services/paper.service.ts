import { BASE_API_URL, METHOD, PAPER_SERVICE_PREDICATE } from "@/type/constant.ts";
import { CreatePaperRequest } from "@/types/paper.types";

const endpoints = ["papers", "references", "tags"] as const;
export type Endpoints = (typeof endpoints)[number];

// <editor-fold> desc="reference"
export const getCitationsByPaperId = async (id: string) => {
    const response = await fetch(`${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[1]}/${id}`, {
        method: METHOD.GET.toString()
    })
    const data = await response.json()
    console.log(data)
    return data;
}
// </editor-fold>

// <editor-fold> desc="paper"
// Helper function to encode userId (base64)
const encodeUserId = (userId: string): string => {
    return btoa(userId);
}

export const getPaperDetails = async (id: string, userId?: string) => {
    const headers: HeadersInit = {
        'Content-Type': 'application/json',
    };
    
    if (userId) {
        headers['X-User-Id'] = encodeUserId(userId);
    }
    
    const response = await fetch(`${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/details?id=${id}`, {
        method: METHOD.GET.toString(),
        headers
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const getReferencesOfPaper = async (id: string) => {
    const response = await fetch(
        `${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/references?id=${id}`,
        { method: METHOD.GET.toString() }
    )
    const data = await response.json()
    console.log(data)
    return data
}

export const getPaginatedPapers = async (
    currentPage: number, pageSize: number,
    kw?: string,
    filter?: {
        author: string,
        journal: string,
        yearFrom: string,
        yearTo: string,
    },
    userId?: string
) => {
    try {
        const pageIndex = Math.max(0, currentPage - 1);

        const params = new URLSearchParams({
            index: pageIndex.toString(),
            size: pageSize.toString(),
            ...(kw ? { search: kw } : {}),
            ...(filter?.author ? { author: filter.author } : {}),
            ...(filter?.journal ? { journal: filter.journal } : {}),
            ...(filter?.yearFrom ? { from: filter.yearFrom } : {}),
            ...(filter?.yearTo ? { to: filter.yearTo } : {}),
        });

        const headers: HeadersInit = {
            'Content-Type': 'application/json',
        };
        
        if (userId) {
            headers['X-User-Id'] = encodeUserId(userId);
        }

        const url = `${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/papers/all?${params.toString()}`;
        const response = await fetch(url, {
            method: METHOD.GET.toString(),
            headers
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to fetch papers' }));
            throw new Error(errorData.message || `Failed to fetch papers: ${response.statusText} (${response.status})`);
        }

        const data = await response.json();
        console.log(data)

        // Check if response has the expected structure
        if (!data || data.status !== 200) {
            throw new Error(data?.message || 'Invalid response from server');
        }

        return data;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Paper Service. Please make sure the service is running.`);
        }
        throw error;
    }
}
// </editor-fold>

export const pingPaperServiceApi = async (endpoint: Endpoints) => {
    const response = await fetch(`${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoint}/health`, {
        method: METHOD.HEAD.toString()
    })
    const status = response.status
    console.log(status)
    return status
}

export const importPaper = async (request: CreatePaperRequest) => {
    const formData = new FormData();

    console.log(request.file)
    formData.append("file", request.file);
    formData.append("title", request.title);
    formData.append("authors", request.authors);
    formData.append("journal", request.journal);
    formData.append("publicationYear", request.publicationYear.toString());

    if (request.doi) formData.append("doi", request.doi);
    if (request.description) formData.append("description", request.description);
    if (request.keywords) formData.append("keywords", request.keywords);
    if (request.tags) formData.append("tags", request.tags);

    try {
        const response = await fetch(`${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/pdf/upload-with-file`, {
            method: METHOD.POST.toString(),
            headers: {
                'X-User-Id': encodeUserId(request.userId),
            },
            body: formData,
        });
        const data = await response.json();
        console.log(data);
        return data;
    } catch (error) {
        console.error("Import paper failed:", error);
    }
}

// Favorite API functions
export const addFavorite = async (paperId: string, userId: string) => {
    try {
        const response = await fetch(
            `${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/${paperId}/favorite`,
            {
                method: METHOD.POST.toString(),
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': encodeUserId(userId),
                }
            }
        );

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to add favorite' }));
            throw new Error(errorData.message || `Failed to add favorite: ${response.statusText} (${response.status})`);
        }

        const data = await response.json();
        console.log(data);
        return data;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Paper Service. Please make sure the service is running.`);
        }
        throw error;
    }
}

export const removeFavorite = async (paperId: string, userId: string) => {
    try {
        const response = await fetch(
            `${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/${paperId}/favorite`,
            {
                method: METHOD.DELETE.toString(),
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': encodeUserId(userId),
                }
            }
        );

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to remove favorite' }));
            throw new Error(errorData.message || `Failed to remove favorite: ${response.statusText} (${response.status})`);
        }

        const data = await response.json();
        console.log(data);
        return data;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Paper Service. Please make sure the service is running.`);
        }
        throw error;
    }
}

export const getFavoritePapers = async (userId: string) => {
    try {
        const response = await fetch(
            `${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/favorites`,
            {
                method: METHOD.GET.toString(),
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': encodeUserId(userId),
                }
            }
        );

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to fetch favorites' }));
            throw new Error(errorData.message || `Failed to fetch favorites: ${response.statusText} (${response.status})`);
        }

        const data = await response.json();
        console.log(data);
        return data;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Paper Service. Please make sure the service is running.`);
        }
        throw error;
    }
}

export const checkFavorite = async (paperId: string, userId: string): Promise<boolean> => {
    try {
        const response = await fetch(
            `${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/${paperId}/favorite/check`,
            {
                method: METHOD.GET.toString(),
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': encodeUserId(userId),
                }
            }
        );

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to check favorite' }));
            throw new Error(errorData.message || `Failed to check favorite: ${response.statusText} (${response.status})`);
        }

        const data = await response.json();
        console.log(data);
        return data?.data?.isFavorite || false;
    } catch (error: any) {
        if (error.name === 'TypeError' && error.message.includes('fetch')) {
            throw new Error(`Cannot connect to Paper Service. Please make sure the service is running.`);
        }
        throw error;
    }
}