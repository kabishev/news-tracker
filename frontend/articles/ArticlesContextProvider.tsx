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
      setStore(setArticles(data))
    } catch (e) {
      console.error(e)
    }
  }

  React.useEffect(() => {
    setStore(setVisitedArticles(JSON.parse(localStorage.getItem('visitedArticleIds') ?? '[]')))
    setStore(setSelectedArticle(localStorage.getItem('selectedArticleId') ?? null))
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
