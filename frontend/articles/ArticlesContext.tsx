import React from 'react'

import { ArticlesStore, makeDefaultArticlesStore } from './articles'

export type ArticlesContextProps = {
  store: ArticlesStore,
  setSelectedArticle: (articleId: string) => void,
  setSelectedLocalization: (code: string) => void,
}

export const ArticlesContext = React.createContext<ArticlesContextProps>({
  store: makeDefaultArticlesStore(),
  setSelectedArticle: () => { },
  setSelectedLocalization: () => { },
})

export const useArticlesContext = () => React.useContext(ArticlesContext)
