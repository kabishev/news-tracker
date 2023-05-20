import React from 'react'
import useWebSocket from 'react-use-websocket'

import { Article, WsEvent } from '@/types/api'

import {
  ArticlesStore,
  makeDefaultArticlesStore,
  setArticles,
  setSelectedArticle,
  setSelectedLocalization,
  setVisitedArticles,
} from './articles'
import { ArticlesContext } from './ArticlesContext'

export const ArticlesContextProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
  const [store, setStore] = React.useState<ArticlesStore>(makeDefaultArticlesStore())
  const { lastMessage } = useWebSocket(
    `${process.env.NEXT_PUBLIC_SERVER_WS_ADDRESS}/ws`, {
    shouldReconnect: () => true,
  });

  const fetchArticles = async () => {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_SERVER_ADDRESS}/api/articles`)
      const data: Article[] = await res.json()
      const sorted = data.sort((a, b) => a.addedAt < b.addedAt ? 1 : -1)
      const articles = sorted.map(article => ({
        ...article,
        createdAt: new Date(article.createdAt).toLocaleDateString(),
      }))
      setStore(setArticles(articles))
      setStore(setSelectedArticle(store.selectedArticleId ?? localStorage.getItem('selectedArticleId') ?? ''))
    } catch (e) {
      console.error(e)
    }
  }

  React.useEffect(() => {
    setStore(setVisitedArticles(JSON.parse(localStorage.getItem('visitedArticleIds') ?? '[]')))
    setStore(setSelectedLocalization(localStorage.getItem('selectedLocalizationCode') ?? 'de'))
    fetchArticles()
  }, [])

  React.useEffect(() => {
    const event: WsEvent | null = JSON.parse(lastMessage?.data || null)
    if (event?.ArticleCreated) {
      fetchArticles()
    }
  }, [lastMessage])

  const value = {
    store,
    setSelectedArticle: (articleId: string) => setStore(prev => {
      const next = setSelectedArticle(articleId)(prev)
      localStorage.setItem('selectedArticleId', next.selectedArticleId || '')
      localStorage.setItem('visitedArticleIds', JSON.stringify(next.visitedArticleIds))
      return next
    }),
    setSelectedLocalization: (code: string) => setStore(prev => {
      const next = setSelectedLocalization(code)(prev)
      localStorage.setItem('selectedLocalizationCode', next.selectedLocalizationCode)
      return next
    }),
  }

  return (
    <ArticlesContext.Provider value={value}>
      {children}
    </ArticlesContext.Provider>
  )
}
