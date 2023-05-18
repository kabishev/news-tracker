import React from 'react'
import Head from 'next/head'

import { MonitoringComponent } from '@/monitoring'
import styles from '@/styles/Home.module.css'

export default function MonitorPage() {

  return (
    <>
      <Head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.png" />
        <title>Monitoring</title>
      </Head>
      <div className={styles.center}>
        <MonitoringComponent />
      </div>
    </>
  )
}
