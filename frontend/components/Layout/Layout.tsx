import React from 'react'
import ArticleIcon from '@mui/icons-material/Article'
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft'
import MenuIcon from '@mui/icons-material/Menu'
import MonitorHeartIcon from '@mui/icons-material/MonitorHeart';
import Box from '@mui/material/Box'
import Divider from '@mui/material/Divider'
import IconButton from '@mui/material/IconButton'
import List from '@mui/material/List'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import Snackbar from '@mui/material/Snackbar'
import Toolbar from '@mui/material/Toolbar'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import Link from 'next/link'

import styles from '@/styles/Layout.module.css'

import { Alert } from './Alert'
import { AppBar } from './AppBar'
import { Drawer } from './Drawer'

const mainListItems = (
  <React.Fragment>
    <Link href="/articles" className={styles.link}>
      <ListItemButton>
        <ListItemIcon><Tooltip title="Articles" placement="right"><ArticleIcon /></Tooltip></ListItemIcon>
        <ListItemText primary="Articles" />
      </ListItemButton>
    </Link>
    <Link href="/monitoring" className={styles.link}>
      <ListItemButton>
        <ListItemIcon><Tooltip title="Monitoring" placement="right"><MonitorHeartIcon /></Tooltip></ListItemIcon>
        <ListItemText primary="Monitoring" />
      </ListItemButton>
    </Link>
  </React.Fragment>
);

export const Layout = ({ children }: React.PropsWithChildren) => {
  const [open, setOpen] = React.useState(true)
  const toggleDrawer = () => { setOpen(!open) }
  const [errorMessage, setErrorMessage] = React.useState<string | undefined>(undefined)

  const handleCloseError = () => setErrorMessage(undefined)

  React.useEffect(() => {
    const checkHealth = async () => {
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_SERVER_ADDRESS}/health/status`)
        if (res.status !== 200) {
          setErrorMessage("Server is not available")
        }
      } catch (e) {
        setErrorMessage("Server is not available")
      }
    }

    const interval = setInterval(checkHealth, 20000)

    return () => clearInterval(interval)
  }, [])

  return (
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
            Evolution Bootcamp - Spring 2023
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
        <Box sx={{ margin: 4 }}>{children}</Box>
      </Box>
      <Snackbar
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
        open={errorMessage !== undefined}
        onClose={handleCloseError}
        message={errorMessage}
        key={'topcenter'}
      >
        <Alert onClose={handleCloseError} severity="error" sx={{ width: '100%' }}>
          {errorMessage}
        </Alert>
      </Snackbar>
    </Box>
  )
}
