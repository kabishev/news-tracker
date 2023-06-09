export interface Article {
  id: string;
  title: string;
  content: string;
  createdAt: string;
  addedAt: string;
  language: string;
  authors: string;
  summary: string | null;
  url: string | null;
  source: string | null;
}
