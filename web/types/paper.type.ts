export interface Paper {
  id?: string;
  dataUrl?: string;
  description?: string;
  keyword?: string[];
  title?: string;
  authors: string;
  journal: string;
  publicationYear: number;
  doi: string;
  tags: string[];
}
