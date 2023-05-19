export type ArticleCreatedEvent = {
  articleId: string;
}

export type ArticleTranslatedEvent = {
  articleId: string;
  language: string;
}

export type ServiceEvent = {
  id: string;
  serviceName: string;
}

export type ServiceOnlineEvent = ServiceEvent;

export type ServiceOfflineEvent = ServiceEvent;

export type ServiceErrorEvent = ServiceEvent & {
  error: string;
}

export type TaskCompletedEvent = ServiceEvent & {
  description: string;
}

export type WsEvent = {
  ArticleCreated?: ArticleCreatedEvent;
  ArticleTranslated?: ArticleTranslatedEvent;
  ServiceOnline?: ServiceOnlineEvent;
  ServiceOffline?: ServiceOfflineEvent;
  ServiceError?: ServiceErrorEvent;
  TaskCompleted?: TaskCompletedEvent;
}