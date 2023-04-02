import * as React from 'react'
import { FixedSizeList, ListChildComponentProps } from 'react-window'
import Box from '@mui/material/Box'
import Grid from '@mui/material/Grid'
import ListItem from '@mui/material/ListItem'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemText from '@mui/material/ListItemText'
import Paper from '@mui/material/Paper'
import { styled } from '@mui/material/styles'

const Article = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2),
  textAlign: "center",
  color: theme.palette.text.secondary,
  height: "100%",
}));

const renderRow = ({ data, index, style }: ListChildComponentProps<string[]>) => (
  <ListItem style={style} key={index} component="div" disablePadding>
    <ListItemButton>
      <ListItemText primary={data[index]} />
    </ListItemButton>
  </ListItem>
);

type ArticleListProps = {
  data: string[]
}

export const ArticleList: React.FC<ArticleListProps> = ({ data }) => (
  <Grid container spacing={3}>
    <Grid item xs={12} md={8}>
     <Article/>
    </Grid>
    <Grid item xs={12} md={4}>
      <Box sx={{ width: "100%", height: 400, maxWidth: "lg", bgcolor: "background.paper" }} >
        <FixedSizeList itemData={data} height={400} width="100%" itemSize={46} itemCount={data.length} overscanCount={5} >
          {renderRow}
        </FixedSizeList>
      </Box>
    </Grid>
  </Grid>
);
