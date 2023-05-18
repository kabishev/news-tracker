import React from 'react'

import { WsEvent } from '@/types/api'

export const WebSocketEventContext = React.createContext<WsEvent | null>(null)
export const useWebSocketEvent = () => React.useContext(WebSocketEventContext)
