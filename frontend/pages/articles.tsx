import React, { useEffect } from 'react'
import CircularProgress from '@mui/material/CircularProgress'
import Head from 'next/head'

import ArticlesComponent from '@/components/Articles'
import { AvailableLanguagesContext } from '@/contexts/AvailableLanguagesContext'
import styles from '@/styles/Home.module.css'
import { Article, ArticleWsEvent } from '@/types/api/article'
import { availableLanguages } from '@/types/languages'

export default function ArticlesPage() {
  const [articles, setArticles] = React.useState<Article[]>([])
  const [isLoading, setLoading] = React.useState(true)

  const fetchArticles = async () => {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_SERVER_ADDRESS}/api/articles`)
      const data: Article[] = await res.json()
      setArticles(data)
      setLoading(false)
    } catch (e) {
      console.error(e)
    }
  }

  useEffect(() => {
    const ws = new WebSocket(`${process.env.NEXT_PUBLIC_SERVER_WS_ADDRESS}/ws`)
    ws.addEventListener('message', (event: MessageEvent) => {
      const message: ArticleWsEvent = JSON.parse(event.data);

      if (message.ArticleCreated) {
        fetchArticles()
      }
    });

    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close()
      }
    }
  }, [])

  useEffect(() => { fetchArticles() }, [])

  return (
    <AvailableLanguagesContext.Provider value={availableLanguages}>
      <Head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.ico" />
        <title>Articles</title>
      </Head>
      <div className={styles.center}>
        {isLoading
          ? <CircularProgress />
          : <ArticlesComponent articles={articles ?? []} />
        }
      </div>
    </AvailableLanguagesContext.Provider>
  )
}
