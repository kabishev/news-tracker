import * as React from 'react'
import CssBaseline from '@mui/material/CssBaseline'
import type { AppProps } from 'next/app'

import Layout from '@/components/Layout'

export default function App({ Component, pageProps }: AppProps) {
  return (
    <React.StrictMode>
      <CssBaseline />
      <Layout>
        <Component {...pageProps} />
      </Layout>
    </React.StrictMode>
  )
}
