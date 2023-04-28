import React, { useEffect } from 'react'
import CircularProgress from '@mui/material/CircularProgress'
import Head from 'next/head'

import Articles from '@/components/Articles'
import styles from '@/styles/Home.module.css'
import { Article } from '@/types/api/article'

export default function ArticlesPage() {
  const [data, setData] = React.useState<Article[]>([])
  const [isLoading, setLoading] = React.useState(true)

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_SERVER_ADDRESS}/api/articles`)
        const data = await res.json()
        setData(data)
        setLoading(false)
      } catch (e) {
        console.error(e)
      }
    }

    fetchData()
  }, [])

  return (
    <>
      <Head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.ico" />
        <title>Articles</title>
      </Head>
      <div className={styles.center}>
        {isLoading
          ? <CircularProgress />
          : <Articles data={data ?? []} />
        }
      </div>
    </>
  )
}
