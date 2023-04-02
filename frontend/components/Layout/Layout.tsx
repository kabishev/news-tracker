import * as React from 'react'
import ArticleIcon from '@mui/icons-material/Article'
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft'
import MenuIcon from '@mui/icons-material/Menu'
import PrecisionManufacturingIcon from '@mui/icons-material/PrecisionManufacturing'
import Box from '@mui/material/Box'
import Container from '@mui/material/Container'
import Divider from '@mui/material/Divider'
import IconButton from '@mui/material/IconButton'
import List from '@mui/material/List'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import { ThemeProvider } from '@mui/material/styles'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import Link from 'next/link'

import styles from '@/styles/Layout.module.css'
import { theme } from '@/styles/theme'

import { AppBar } from './AppBar'
import { Drawer } from './Drawer'

const mainListItems = (
  <React.Fragment>
    <Link href="/articles" className={styles.link}>
      <ListItemButton>
        <ListItemIcon><ArticleIcon /></ListItemIcon>
        <ListItemText primary="Articles" />
      </ListItemButton>
    </Link>
    <Link href="/bots" className={styles.link}>
      <ListItemButton>
        <ListItemIcon><PrecisionManufacturingIcon /></ListItemIcon>
        <ListItemText primary="Bots" />
      </ListItemButton>
    </Link>
  </React.Fragment>
);

export const Layout = ({ children }: React.PropsWithChildren) => {
  const [open, setOpen] = React.useState(true)
  const toggleDrawer = () => { setOpen(!open) }

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{ display: "flex" }}>
        <AppBar position="absolute" open={open}>
          <Toolbar sx={{ pr: "24px" }}>
            <IconButton
              edge="start"
              color="inherit"
              aria-label="open drawer"
              onClick={toggleDrawer}
              sx={{
                marginRight: "36px",
                ...(open && { display: "none" }),
              }}
            >
              <MenuIcon />
            </IconButton>
            <Typography component="h1" variant="h6" color="inherit" noWrap sx={{ flexGrow: 1 }}>
              Evolution bootcamp 2023
            </Typography>
          </Toolbar>
        </AppBar>
        <Drawer variant="permanent" color='primary' open={open}>
          <Toolbar sx={{ display: "flex", alignItems: "center", justifyContent: "flex-end", px: [1], }} >
            <IconButton onClick={toggleDrawer}>
              <ChevronLeftIcon />
            </IconButton>
          </Toolbar>
          <Divider />
          <List component="nav">
            {mainListItems}
          </List>
        </Drawer>
        <Box
          component="main"
          sx={{
            backgroundColor: (theme) => theme.palette.mode === 'light' ? theme.palette.grey[100] : theme.palette.grey[900],
            flexGrow: 1,
            height: '100vh',
            overflow: 'auto',
          }}
        >
          <Container sx={{ mt: 4, mb: 4 }}>{children}</Container>
        </Box>
      </Box>
    </ThemeProvider>
  )
}
