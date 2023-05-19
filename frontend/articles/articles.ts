import { Article, Translation } from "@/types/api";

export type ArticlesStore = {
  ready: boolean;
  articles: Article[];
  visitedArticleIds: string[];
  selectedArticleId: string | null;
  translation: Translation | null;
  selectedLocalizationCode: string;
}

export const makeDefaultArticlesStore = (): ArticlesStore => ({
  ready: false,
  articles: [],
  visitedArticleIds: [],
  selectedArticleId: null,
  translation: null,
  selectedLocalizationCode: 'de',
});

export const setArticles = (articles: Article[]) => (prev: ArticlesStore): ArticlesStore =>
  ({ ...prev, articles, ready: true });

export const setVisitedArticles = (articleIds: string[]) => (prev: ArticlesStore): ArticlesStore => ({
  ...prev,
  visitedArticleIds: articleIds,
});

export const setSelectedArticle = (articleId: string | null) => (prev: ArticlesStore): ArticlesStore =>
  prev.articles.find(article => article.id === articleId)
    ? ({
      ...prev,
      selectedArticleId: articleId,
      visitedArticleIds: articleId
        ? [...((prev.visitedArticleIds).filter(id => id !== articleId)), articleId]
        : prev.visitedArticleIds,
    }) : ({
      ...prev,
      selectedArticleId: null,
    })


export const setTranslation = (
  translation: Translation,
  localizationCode: string = 'de'
) => (prev: ArticlesStore): ArticlesStore => ({
  ...prev,
  translation,
  selectedLocalizationCode: localizationCode,
})

export const setSelectedLocalization = (code: string) => (prev: ArticlesStore): ArticlesStore => ({
  ...prev,
  selectedLocalizationCode: code,
})
