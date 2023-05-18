import React, { useEffect } from 'react'
import useWebSocket from 'react-use-websocket'
import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress'
import Paper from '@mui/material/Paper'
import { styled } from '@mui/material/styles'

import { WsEvent } from '@/types/api'
import { Translation } from '@/types/api/translation'

import { Localizations } from './Localizations'

type TranslationComponentProps = {
  articleId?: string
}

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

export const TranslationComponent: React.FC<TranslationComponentProps> = (props) => {
  const [translation, setTranslation] = React.useState<Translation>()
  const [waitTranslation, setWaitTranslation] = React.useState<string>()
  const [selectedCode, setSelectedCode] = React.useState<string>()
  const { lastMessage } = useWebSocket(`${process.env.NEXT_PUBLIC_SERVER_WS_ADDRESS}/ws`);

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

  const translateContent = async (id: string, language: string) => {
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
      console.error(e)
    }
  }

  useEffect(() => setSelectedCode(localStorage.getItem('selectedCode') ?? 'en'), [])

  useEffect(() => {
    const event: WsEvent | null = JSON.parse(lastMessage?.data || null)
    if (event?.ArticleTranslated) {
      const { articleId } = event.ArticleTranslated
      if (articleId === props.articleId) {
        fetchTranslation(articleId)
      }
    }
  }, [lastMessage, props.articleId])

  React.useEffect(() => {
    if (props.articleId) {
      fetchTranslation(props.articleId)
    }
  }, [props.articleId])

  React.useEffect(() => setWaitTranslation(undefined), [translation, selectedCode])

  React.useEffect(() => {
    if (!props.articleId || !translation || !selectedCode) {
      return
    }

    const localization = translation
      .localizations
      .find(({ language }) => language.toLowerCase() === selectedCode.toLowerCase())

    if (!localization) {
      translateContent(props.articleId, selectedCode)
    }
  }, [props.articleId, translation, selectedCode])

  const localizations = (translation && translation.localizations) ?? []
  const content = localizations.find(({ language }) => language.toLowerCase() === selectedCode?.toLowerCase())?.content

  const handleTabChanged = (code: string) => {
    setSelectedCode(code)
    localStorage.setItem('selectedCode', code)
  }

  return (
    <>
      <Localizations selectedCode={selectedCode} localizations={localizations} onTabChanged={handleTabChanged} />
      <ArticleContent>
        {!waitTranslation
          ? <MarkupText markup={content} />
          : <Progress />}
      </ArticleContent >
    </>
  );
}
