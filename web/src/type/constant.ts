export const BASE_API_URL = "http://localhost:8080"
export const PAPER_SERVICE_PREDICATE = "paper-service"
export const GROUP_SERVICE_PREDICATE = "group-service"
export const ACCOUNT_SERVICE_PREDICATE = "account-service"
export const ACCOUNT_SERVICE_URL = `${BASE_API_URL}/${ACCOUNT_SERVICE_PREDICATE}` // 
export const READING_SERVICE_PREDICATE = "reading-service"

export enum METHOD {
    GET = "GET",
    POST = "POST",
    PUT = "PUT",
    PATCH = "PATCH",
    DELETE = "DELETE",
    HEAD = "HEAD"
}