import React from 'react'
import Head from 'next/head'

import { ArticlesComponent } from '@/articles'
import { AvailableLanguagesContext } from '@/contexts/AvailableLanguagesContext'
import styles from '@/styles/Home.module.css'
import { availableLanguages } from '@/types/languages'

export default function ArticlesPage() {
  return (
    <AvailableLanguagesContext.Provider value={availableLanguages}>
      <Head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.png" />
        <title>Articles</title>
      </Head>
      <div className={styles.center}>
        <ArticlesComponent />
      </div>
    </AvailableLanguagesContext.Provider>
  )
}
