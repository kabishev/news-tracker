import * as React from 'react'
import ReactCountryFlag from 'react-country-flag'
import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import Typography from '@mui/material/Typography'

import { useAvailableLanguages } from '@/contexts/AvailableLanguagesContext'
import { Localization } from '@/types/api/translation'
import { Language } from '@/types/languages'

const lable = (code: string, name: string, marked: boolean) => {
  const countryCode = code && code.toLowerCase() === "en" ? "us" : code;
  return (
    <Grid container direction="row" alignItems="center" spacing={1}>
      <Grid item>
        <ReactCountryFlag countryCode={countryCode} />
      </Grid>
      <Grid item>
        <Typography variant='body2' style={{ fontWeight: marked ? 'bold' : "normal" }} >{name}</Typography>
      </Grid>
    </Grid>
  )
}

type LocalizationsProps = {
  localizations: Localization[]
  onTabChanged: (code: string) => void
}

export const Localizations: React.FC<LocalizationsProps> = ({ localizations, onTabChanged }) => {
  const [value, setValue] = React.useState(0)
  const languages = useAvailableLanguages()
  const translatedCodes = localizations.map(({ language }) => language.toLowerCase())
  const marked = (language: Language) => translatedCodes.includes(language.code.toLowerCase())

  const handleChange = (_: React.SyntheticEvent, value: number) => {
    console.log(`Tab changed to ${value}`)
    setValue(value)
    onTabChanged(languages[value].code)
  }

  React.useEffect(() => {
    setValue(0)
  }, [localizations])

  return (
    <>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={value} onChange={handleChange}>
          {languages.map((language, index) =>
            <Tab
              key={`${index}${language.code}`}
              label={lable(language.code, language.name, marked(language))}
            />
          )}
        </Tabs>
      </Box>
    </>
  )
}
