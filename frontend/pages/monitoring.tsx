import Head from 'next/head'

import styles from '@/styles/Home.module.css'

export default function MonitorPage() {
  return (
    <>
      <Head>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link rel="icon" href="/favicon.ico" />
        <title>Monitoring</title>
      </Head>
      <div className={styles.center}>Monitor</div>
    </>
  )
}