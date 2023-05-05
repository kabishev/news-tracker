import React, { useEffect } from 'react'
import Head from 'next/head'
import { GetServerSideProps } from 'next/types'

import Articles from '@/components/Articles'
import styles from '@/styles/Home.module.css'
import { Article } from '@/types/api/article'

export default function ArticlesPage({ data }: { data: Article[] }) {
  return (
    <>
      <Head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.ico" />
        <title>Articles</title>
      </Head>
      <div className={styles.center}>
        <Articles data={data ?? []} />
      </div>
    </>
  )
}

export const getServerSideProps: GetServerSideProps = async () => {
  const res = await fetch(`${process.env.SERVER_ADDRESS}/api/articles`)
  const data = await res.json()
  return { props: { data } }
}