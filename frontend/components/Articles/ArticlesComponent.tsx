import * as React from 'react'
import ReactCountryFlag from 'react-country-flag'
import { FixedSizeList, ListChildComponentProps } from 'react-window'
import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemText from '@mui/material/ListItemText'
import { useTheme } from '@mui/material/styles'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import useMediaQuery from '@mui/material/useMediaQuery'

import { Article } from '@/types/api/article'

import { TranslationComponent } from './TranslationComponent'

const renderTitleItem = (
  selectedItem: number,
  viewedItems: string[],
  onClick: (id: number) => void
) => ({ data, index, style }: ListChildComponentProps<Article[]>) => {
  const { id, title, source, authors, createdAt, language } = data[index];
  const countryCode = language && language.toLowerCase() === "en" ? "us" : language;
  const description = [source, authors, createdAt].filter((item) => item).join(' - ');
  const marked = viewedItems.includes(id);
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
            <Grid item container xs={12}>
              <Grid item xs={1}>
                {language && <ReactCountryFlag countryCode={countryCode} />}
              </Grid>
              <Grid item xs={11}>
                <Typography
                  variant="caption"
                  color={marked ? "text.disabled" : "text.secondary"}
                >
                  {description}
                </Typography>
              </Grid>
            </Grid>
          </Grid>
        } />
      </Tooltip>
    </ListItemButton>
  ) as JSX.Element;
}

type ArticlesComponentProps = {
  articles: Article[]
}

export const ArticlesComponent: React.FC<ArticlesComponentProps> = ({ articles }) => {
  const theme = useTheme();
  const matches = useMediaQuery(theme.breakpoints.up('md'));
  const [selectedItem, setSelectedItem] = React.useState<number>(0);
  const [viewedItems, setViewedItems] = React.useState<string[]>([]);

  React.useEffect(() => {
    const storedItems: string[] = JSON.parse(localStorage.getItem('viewedItems') ?? '[]');
    if (storedItems) {
      setViewedItems(storedItems);
    }
  }, []);

  React.useEffect(() => {
    const storedId = localStorage.getItem('selectedArticleId');
    if (storedId) {
      const index = articles.findIndex((article) => article.id === storedId);
      setSelectedItem(index > -1 ? index : 0);
    }

  }, [articles]);

  const handleListItemClick = (item: number) => {
    setSelectedItem(item);

    if (!viewedItems.includes(articles[item].id)) {
      const newViewedItems = [...viewedItems, articles[item].id];
      setViewedItems(newViewedItems);
      localStorage.setItem('viewedItems', JSON.stringify(newViewedItems));
    }

    localStorage.setItem("selectedArticleId", articles[item].id);
  }

  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <Box sx={{ width: "100%", bgcolor: "background.paper" }} >
          <FixedSizeList
            itemData={articles}
            height={matches ? 1000 : 200}
            width="100%"
            itemSize={80}
            itemCount={articles.length}
            overscanCount={5} >
            {renderTitleItem(selectedItem, viewedItems, handleListItemClick)}
          </FixedSizeList>
        </Box>
      </Grid>
      <Grid item xs={12} md={8}>
        <TranslationComponent articleId={articles[selectedItem].id} />
      </Grid>
    </Grid>
  );
}
