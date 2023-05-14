import * as React from 'react'
import Paper from '@mui/material/Paper'
import { styled } from '@mui/material/styles'

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

export const TranslationComponent: React.FC<TranslationComponentProps> = ({ articleId }) => {
  const [translation, setTranslation] = React.useState<Translation>()
  const [content, setContent] = React.useState<string>()

  React.useEffect(() => {
    if (articleId) {
      fetchTranslation(articleId)
    }
  }, [articleId])

  React.useEffect(() => {
    const c = translation && translation.localizations[0].content
    setContent(c)
  }, [translation])

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

  const handleTabChanged = (code: string) => {
    const localizations = translation?.localizations ?? []
    const localization = localizations.find(({ language }) => language.toLowerCase() === code.toLowerCase())
    const content = localization?.content
    setContent(content)
  }

  const localizations = (translation && translation.localizations) ?? []

  return (
    <>
      <Localizations localizations={localizations} onTabChanged={handleTabChanged} />
      <ArticleContent>
        <MarkupText markup={content} />
      </ArticleContent >
    </>
  ) || null;
}
