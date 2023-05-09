package newstracker.article.domain

final case class CreateArticle(
    title: ArticleTitle,
    content: ArticleContent,
    createdAt: ArticleCreatedAt,
    language: ArticleLanguage,
    authors: ArticleAuthors,
    summary: Option[ArticleSummary],
    url: Option[ArticleUrl],
    source: Option[ArticleSource],
    tags: Option[ArticleTags]
)
