import * as React from 'react'
import { FixedSizeList, ListChildComponentProps } from 'react-window'
import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import ListItem from '@mui/material/ListItem'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemText from '@mui/material/ListItemText'
import Paper from '@mui/material/Paper'
import { styled, Theme, useTheme } from '@mui/material/styles'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import useMediaQuery from '@mui/material/useMediaQuery';

import { Article } from '@/types/api/article'

const ArticleComponent = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2),
  color: theme.palette.text.secondary,
  height: "100%",
}));

const renderTitleItem = (
  selectedItem: number,
  viewedItems: string[],
  onClick: (id: number) => void
) => ({ data, index, style }: ListChildComponentProps<Article[]>) => {
  const { id, title, source, authors, createdAt } = data[index];
  const marked = viewedItems.includes(id);
  return (
    <ListItem style={style} key={index} component="div" disablePadding>
      <ListItemButton selected={selectedItem == index} onClick={() => onClick(index)}>
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
              <Grid item xs={12}>
                <Typography
                  variant="caption"
                  color={marked ? "text.disabled" : "text.secondary"}
                >
                  {`${source} - ${authors} - ${createdAt}`}
                </Typography>
              </Grid>
            </Grid>
          } />
        </Tooltip>
      </ListItemButton>
    </ListItem>
  ) as JSX.Element;
}

const MarkupText: React.FC<{ markup: string }> = ({ markup }) => <div dangerouslySetInnerHTML={{ __html: markup }} />;

type ArticleListProps = {
  data: Article[]
}

export const ArticleList: React.FC<ArticleListProps> = ({ data }) => {
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

  const handleListItemClick = (item: number) => {
    setSelectedItem(item);
    if (!viewedItems.includes(data[item].id)) {
      const newViewedItems = [...viewedItems, data[item].id];
      setViewedItems(newViewedItems);
      localStorage.setItem('viewedItems', JSON.stringify(newViewedItems));
    }
  }

  return (
    <Grid container spacing={1}>
      <Grid item xs={12} md={4}>
        <Box sx={{ width: "100%", bgcolor: "background.paper" }} >
          <FixedSizeList itemData={data} height={matches ? 1000 : 200} width="100%" itemSize={64} itemCount={data.length} overscanCount={5} >
            {renderTitleItem(selectedItem, viewedItems, handleListItemClick)}
          </FixedSizeList>
        </Box>
      </Grid>
      <Grid item xs={12} md={8}>
        <ArticleComponent>
          {data.length !== 0 && <MarkupText markup={data[selectedItem].content} />}
        </ArticleComponent>
      </Grid>
    </Grid>
  );
}
