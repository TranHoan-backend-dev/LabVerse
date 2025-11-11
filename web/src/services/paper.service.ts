import { BASE_API_URL, METHOD, PAPER_SERVICE_PREDICATE } from "@/type/constant.ts";

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
export const getPaperDetails = async (id: string) => {
    const response = await fetch(`${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/details?id=${id}`, {
        method: METHOD.GET.toString()
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
    }) => {
    try {
        // Convert 1-based page to 0-based index for Spring Data
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

        const url = `${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/papers/all?${params.toString()}`;
        const response = await fetch(url, { 
            method: METHOD.GET.toString(),
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: 'Failed to fetch papers' }));
            throw new Error(errorData.message || `Failed to fetch papers: ${response.statusText} (${response.status})`);
        }

        const data = await response.json();
        
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