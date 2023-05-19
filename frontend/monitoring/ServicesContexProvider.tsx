import React from 'react'
import useWebSocket from 'react-use-websocket'

import { WsEvent } from '@/types/api'

import { addServiceMessage, Service } from './services'
import { ServicesContext } from './ServicesContext'

export const ServicesContextProvider: React.FC<React.PropsWithChildren<{}>> = ({ children }) => {
  const [services, setServices] = React.useState<Service[]>([])
  const { lastMessage } = useWebSocket(
    `${process.env.NEXT_PUBLIC_SERVER_WS_ADDRESS}/ws`, {
    shouldReconnect: () => true,
  });

  React.useEffect(() => {
    const event: WsEvent | null = JSON.parse(lastMessage?.data || null)
    if (!event) {
      return
    }

    if (event.ServiceOnline) {
      const ev = event.ServiceOnline
      setServices(x => addServiceMessage(ev.serviceName, ev.id, `Service is online`, 'info')(x))
    }

    if (event.ServiceOffline) {
      const ev = event.ServiceOffline
      setServices(addServiceMessage(ev.serviceName, ev.id, `Service is offline`, 'info', false))
    }

    if (event.ServiceError) {
      const ev = event.ServiceError
      setServices(addServiceMessage(ev.serviceName, ev.id, ev.error, 'error'))
    }

    if (event.TaskCompleted) {
      const ev = event.TaskCompleted
      setServices(addServiceMessage(ev.serviceName, ev.id, ev.description, 'success'))
    }

  }, [lastMessage])

  return (
    <ServicesContext.Provider value={services}>
      {children}
    </ServicesContext.Provider>
  )
}