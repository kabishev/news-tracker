import React, { useEffect } from 'react'
import useWebSocket from 'react-use-websocket'
import CircularProgress from '@mui/material/CircularProgress'
import Head from 'next/head'

import ArticlesComponent from '@/components/Articles'
import { AvailableLanguagesContext } from '@/contexts/AvailableLanguagesContext'
import styles from '@/styles/Home.module.css'
import { Article, WsEvent } from '@/types/api'
import { availableLanguages } from '@/types/languages'

export default function ArticlesPage() {
  const [articles, setArticles] = React.useState<Article[]>([])
  const [isLoading, setLoading] = React.useState(true)
  const { lastMessage } = useWebSocket(`${process.env.NEXT_PUBLIC_SERVER_WS_ADDRESS}/ws`);

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
    const event: WsEvent | null = JSON.parse(lastMessage?.data || null)
    if (event?.ArticleCreated) {
      fetchArticles()
    }
  }, [lastMessage])

  useEffect(() => { fetchArticles() }, [])

  return (
    <AvailableLanguagesContext.Provider value={availableLanguages}>
      <Head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.png" />
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
