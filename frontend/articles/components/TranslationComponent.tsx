import React, { useEffect } from 'react'
import useWebSocket from 'react-use-websocket'
import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress'
import Paper from '@mui/material/Paper'
import { styled } from '@mui/material/styles'

import { WsEvent } from '@/types/api'
import { Translation } from '@/types/api/translation'

import { useArticlesContext } from '../ArticlesContext'

import { Localizations } from './Localizations'

const ArticleContent = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2),
  color: theme.palette.text.secondary,
  height: "100%",
}));

const MarkupText: React.FC<{ markup?: string }> = ({ markup }) =>
  markup && <div dangerouslySetInnerHTML={{ __html: markup }} /> || null

const Progress: React.FC = () => (
  <Box sx={{ display: 'flex', justifyContent: 'center' }}>
    <CircularProgress />
  </Box>
)

export const TranslationComponent: React.FC = () => {
  const [translation, setTranslation] = React.useState<Translation | undefined>()
  const [waitTranslation, setWaitTranslation] = React.useState<string>()
  const { store, setSelectedLocalization } = useArticlesContext()
  const { selectedArticleId, selectedLocalizationCode } = store

  const { lastMessage } = useWebSocket(
    `${process.env.NEXT_PUBLIC_SERVER_WS_ADDRESS}/ws`, {
    shouldReconnect: () => true,
  });

  const fetchTranslation = async (id: string) => {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_SERVER_ADDRESS}/api/translations/${id}`)
      const data: Translation = await res.json()
      setTranslation(data)
    } catch (e) {
      console.error(e)
      setTranslation(undefined)
    }
  }

  const translateContent = React.useCallback(async (id: string, language: string) => {
    try {
      setWaitTranslation(language)
      await fetch(`${process.env.NEXT_PUBLIC_SERVER_ADDRESS}/api/translations/${id}/localizations/${language}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      })
    }
    catch (e) {
      setWaitTranslation(selectedLocalizationCode)
      console.error(e)
    }
  }, [selectedLocalizationCode])

  useEffect(() => {
    const event: WsEvent | null = JSON.parse(lastMessage?.data || null)
    if (event?.ArticleTranslated) {
      const { articleId } = event.ArticleTranslated
      if (articleId === selectedArticleId) {
        fetchTranslation(articleId)
      }
    }
  }, [lastMessage, selectedArticleId])

  React.useEffect(() => {
    if (selectedArticleId !== null) {
      fetchTranslation(selectedArticleId)
    }
  }, [selectedArticleId])

  React.useEffect(() => setWaitTranslation(undefined), [translation, selectedLocalizationCode])

  React.useEffect(() => {
    if (selectedArticleId === null || !translation) {
      return
    }

    const localization = translation
      ?.localizations
      ?.find(({ language }) => language.toLowerCase() === selectedLocalizationCode.toLowerCase())

    if (!localization) {
      translateContent(selectedArticleId, selectedLocalizationCode)
    }
  }, [selectedArticleId, translation, selectedLocalizationCode, translateContent])

  const localizations = (translation && translation.localizations) ?? []
  const content = localizations.find(({ language }) => language.toLowerCase() === selectedLocalizationCode.toLowerCase())?.content

  const handleTabChanged = (code: string) => setSelectedLocalization(code)

  return (
    <>
      <Localizations selectedCode={selectedLocalizationCode} localizations={localizations} onTabChanged={handleTabChanged} />
      <ArticleContent>
        {!waitTranslation
          ? <MarkupText markup={content} />
          : <Progress />}
      </ArticleContent >
    </>
  );
}
