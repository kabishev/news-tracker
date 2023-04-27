import * as React from 'react'
import { FixedSizeList, ListChildComponentProps } from 'react-window'
import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import ListItem from '@mui/material/ListItem'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemText from '@mui/material/ListItemText'
import Paper from '@mui/material/Paper'
import { styled, useTheme } from '@mui/material/styles'
import Typography from '@mui/material/Typography'
import useMediaQuery from '@mui/material/useMediaQuery';

import { Article } from '@/types/api/article'

const ArticleComponent = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2),
  color: theme.palette.text.secondary,
  height: "100%",
}));

const renderTitleItem = (onClick: (id: number) => void) => ({ data, index, style }: ListChildComponentProps<Article[]>) => (
  <ListItem style={style} key={index} component="div" disablePadding>
    <ListItemButton onClick={() => onClick(index)}>
      <ListItemText primary={
        <Grid container direction="column">
          <Grid item xs={12} width="100%">
            <Typography noWrap variant="subtitle2" color="text.primary" >
              {data[index].title}
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="caption" color="text.secondary" >
              {`${data[index].source} - ${data[index].authors} - ${data[index].createdAt}`}
            </Typography>
          </Grid>
        </Grid>
      } />
    </ListItemButton>
  </ListItem>
) as JSX.Element;

const MarkupText: React.FC<{ markup: string }> = ({ markup }) => <div dangerouslySetInnerHTML={{ __html: markup }} />;

type ArticleListProps = {
  data: Article[]
}

export const ArticleList: React.FC<ArticleListProps> = ({ data }) => {
  const theme = useTheme();
  const matches = useMediaQuery(theme.breakpoints.up('md'));
  const [selectedItem, setSelectedItem] = React.useState<number | undefined>(undefined);

  const handleListItemClick = (item: number) => setSelectedItem(item);

  return (
    <Grid container spacing={1}>
      <Grid item xs={12} md={4}>
        <Box sx={{ width: "100%", bgcolor: "background.paper" }} >
          <FixedSizeList itemData={data} height={matches ? 1000 : 200} width="100%" itemSize={64} itemCount={data.length} overscanCount={5} >
            {renderTitleItem(handleListItemClick)}
          </FixedSizeList>
        </Box>
      </Grid>
      <Grid item xs={12} md={8}>
        <ArticleComponent>
          {selectedItem !== undefined && <MarkupText markup={data[selectedItem].content} />}
        </ArticleComponent>
      </Grid>
    </Grid>
  );
}
