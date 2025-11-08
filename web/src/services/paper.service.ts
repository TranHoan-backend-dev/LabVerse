import {BASE_API_URL, METHOD, PAPER_SERVICE_PREDICATE} from "@/type/constant.ts";

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
        {method: METHOD.GET.toString()}
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
    const response = await fetch(
        `${BASE_API_URL}/${PAPER_SERVICE_PREDICATE}/${endpoints[0]}/all?index=${currentPage}&size=${pageSize}
        ${kw && `search=${kw}`}
        ${filter.author && `&author=${filter.author}`}
        ${filter.journal && `&journal=${filter.journal}`}
        ${filter.yearFrom && `&from=${filter.yearFrom}`}
        ${filter.yearTo && `&to=${filter.yearTo}`}`,
        {method: METHOD.GET.toString()}
    )
    const data = await response.json()
    console.log(data)
    return data
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