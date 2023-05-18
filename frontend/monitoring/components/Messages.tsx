import React from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Fade from '@mui/material/Fade'
import Stack from '@mui/material/Stack'

import { ServiceMessage } from '../services'

export type MessageType = 'info' | 'error' | 'success'

export type Message = ServiceMessage & {
  ref: React.RefObject<HTMLDivElement>
  in: boolean
}

type MessagesProps = {
  messages: ServiceMessage[]
}

export const Messages: React.FC<MessagesProps> = ({ messages }) => {

  return (
    <Stack sx={{ width: '100%' }} spacing={1}>
      {messages.map((m) => {
        const ref = React.createRef<HTMLDivElement>()
        const time = new Date(m.timestamp).toLocaleTimeString('en-gb')
        return (
          <Box key={m.id} ref={ref}>
            <Fade in={true} timeout={600} >
              <Alert ref={ref} icon={false} variant="outlined" severity={m.type}>
                {`${time}: ${m.message}`}
              </Alert>
            </Fade>
          </Box>
        )
      })}
    </Stack>
  );
}
