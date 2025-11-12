export interface CreatePaperRequest {
    file: File;
    title: string;
    authors: string;
    journal: string,
    publicationYear: number;
    doi: string,
    description?: string | null;
    keywords?: string | null;
    tags?: string | null;
    userId: string;
}