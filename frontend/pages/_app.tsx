import * as React from 'react'
import CssBaseline from '@mui/material/CssBaseline'
import { ThemeProvider } from '@mui/material/styles'
import type { AppProps } from 'next/app'

import { ArticlesContextProvider } from '@/articles'
import Layout from '@/components/Layout'
import { ServicesContextProvider } from '@/monitoring'
import { theme } from '@/styles/theme'

export default function App({ Component, pageProps }: AppProps) {
  return (
    <React.StrictMode>
      <CssBaseline />
      <ThemeProvider theme={theme}>
        <ArticlesContextProvider>
          <ServicesContextProvider>
            <Layout>
              <Component {...pageProps} />
            </Layout>
          </ServicesContextProvider>
        </ArticlesContextProvider>
      </ThemeProvider>
    </React.StrictMode>
  )
}
