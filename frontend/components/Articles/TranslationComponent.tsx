import * as React from 'react'
import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress'
import Paper from '@mui/material/Paper'
import { styled } from '@mui/material/styles'

import { ArticleWsEvent } from '@/types/api/article'
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

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

export const TranslationComponent: React.FC<TranslationComponentProps> = (props) => {
  const [translation, setTranslation] = React.useState<Translation>()
  const [waitTranslation, setWaitTranslation] = React.useState<string>()
  const [selectedCode, setSelectedCode] = React.useState<string>()

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

  React.useEffect(() => {
    const ws = new WebSocket(`${process.env.NEXT_PUBLIC_SERVER_WS_ADDRESS}/ws`)
    ws.addEventListener('message', (event: MessageEvent) => {
      const message: ArticleWsEvent = JSON.parse(event.data);

      if (message.ArticleTranslated) {
        const { articleId } = message.ArticleTranslated
        if (articleId === props.articleId) {
          fetchTranslation(articleId)
        }
      }
    });

    if (props.articleId) {
      fetchTranslation(props.articleId)
    }

    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close()
      }
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

  return (
    <>
      <Localizations
        selectedCode={selectedCode}
        localizations={localizations}
        onTabChanged={setSelectedCode}
      />
      <ArticleContent>
        {!waitTranslation
          ? <MarkupText markup={content} />
          : <Progress />}
      </ArticleContent >
    </>
  ) || null;
}
