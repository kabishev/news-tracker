import React from 'react'

import { Language } from '@/types/languages'

export const AvailableLanguagesContext = React.createContext<Language[]>([])
export const useAvailableLanguages = () => React.useContext(AvailableLanguagesContext)
