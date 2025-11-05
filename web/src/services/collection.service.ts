import {BASE_API_URL, GROUP_SERVICE_PREDICATE, METHOD} from "@/type/constant.ts";

const endpoints = ["collections", "collections/members"] as const;

// <editor-fold> desc="collections"
export const createNewCollection = async () => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const getDetailsById = async (id: string) => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${id}`, {
        method: METHOD.GET.toString(),
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const getPaginatedCollections = async (currentPage: number, pageSize?: number) => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}?page=${currentPage}&size=${pageSize}`, {
        method: METHOD.GET.toString(),
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const getMyCollections = async (userId: string) => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/my?userId=${userId}`, {
        method: METHOD.GET.toString(),
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const getSharedCollections = async (userId: string) => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/shared?userId=${userId}`, {
        method: METHOD.GET.toString(),
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const addPapers = async () => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/papers/`, {
        method: METHOD.GET.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const updateStatusOfPaper = async () => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/papers/status`, {
        method: METHOD.PUT.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const getPapersOfCollection = async (id: string) => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[0]}/${id}/papers`, {
        method: METHOD.GET.toString()
    })
    const data = await response.json()
    console.log(data)
    return data
}
// </editor-fold>

// <editor-fold> desc="User's collection"
export const addMember = async () => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}/`, {
        method: METHOD.POST.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const deleteMember = async (collectionId: string, memberId: string) => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}/${collectionId}/${memberId}`, {
        method: METHOD.DELETE.toString(),
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
    })
    const data = await response.json()
    console.log(data)
    return data
}

export const getMembersList = async (collectionId: string) => {
    const response = await fetch(`${BASE_API_URL}/${GROUP_SERVICE_PREDICATE}/${endpoints[1]}/${collectionId}`, {
        method: METHOD.DELETE.toString()
    })
    const data = await response.json()
    console.log(data)
    return data
}
// </editor-fold>