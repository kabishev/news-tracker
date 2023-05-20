import * as React from 'react'
import ReactCountryFlag from 'react-country-flag'
import { FixedSizeList, ListChildComponentProps } from 'react-window'
import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress'
import Grid from '@mui/material/Grid'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemText from '@mui/material/ListItemText'
import { useTheme } from '@mui/material/styles'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import useMediaQuery from '@mui/material/useMediaQuery'

import { Article } from '@/types/api/article'

import { useArticlesContext } from '../ArticlesContext'

import { TranslationComponent } from './TranslationComponent'

const renderTitleItem = (
  selectedItem: number,
  visited: string[],
  matches: boolean,
  onClick: (id: number) => void
) => ({ data, index, style }: ListChildComponentProps<Article[]>) => {
  const { id, title, source, authors, createdAt, language } = data[index]
  const countryCode = language && language.toLowerCase() === "en" ? "us" : language
  const description = [source, authors, createdAt].filter((item) => item).join(' - ')
  const marked = visited.includes(id)

  return (
    <ListItemButton divider key={index} style={style} selected={selectedItem == index} onClick={() => onClick(index)}>
      <Tooltip title={title} placement='right'>
        <ListItemText primary={
          <Grid container direction="column">
            <Grid item xs={12} width="100%">
              <Typography
                noWrap
                variant="subtitle2"
                color={marked ? "text.disabled" : "text.primary"}
                fontWeight={marked ? 'none' : 'bold'}
              >
                {title}
              </Typography>
            </Grid>
            { matches &&
              <Grid item container xs={12}>
                <Grid item xs={1}>
                  {language && <ReactCountryFlag countryCode={countryCode} />}
                </Grid>
                <Grid item xs={11}>
                  <Typography variant="caption" color={marked ? "text.disabled" : "text.secondary"}>
                    {description}
                  </Typography>
                </Grid>
              </Grid>
            }
          </Grid>
        } />
      </Tooltip>
    </ListItemButton>
  ) as JSX.Element;
}

export const ArticlesComponent: React.FC = () => {
  const theme = useTheme();
  const matches = useMediaQuery(theme.breakpoints.up('md'))

  const { store, setSelectedArticle } = useArticlesContext()
  const { ready, articles, visitedArticleIds, selectedArticleId } = store

  const handleListItemClick = React.useCallback(
    (item: number) => setSelectedArticle(articles[item].id),
    [articles, setSelectedArticle])

  const selected = articles.findIndex((article) => article.id === selectedArticleId)

  return !ready ? <CircularProgress /> : (
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <Box sx={{ width: "100%", bgcolor: "background.paper" }} >
          <FixedSizeList
            itemData={articles}
            height={matches ? 700 : 200}
            width="100%"
            itemSize={matches ? 80 : 40}
            itemCount={articles.length}
            overscanCount={5} >
            {renderTitleItem(selected, visitedArticleIds, matches, handleListItemClick)}
          </FixedSizeList>
        </Box>
      </Grid>
      <Grid item xs={12} md={8}>
        <TranslationComponent />
      </Grid>
    </Grid>
  );
}
