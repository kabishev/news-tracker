export interface Article {
  id: string;
  title: string;
  content: string;
  createdAt: string;
  language: string;
  authors: string;
  summary: string | null;
  url: string | null;
  source: string | null;
}

export type ArticleCreatedEvent = {
  articleId: string;
}

export type ArticleTranslatedEvent = {
  articleId: string;
  language: string;
}

export type ArticleWsEvent = {
  ArticleCreated?: ArticleCreatedEvent;
  ArticleTranslated?: ArticleTranslatedEvent;
}