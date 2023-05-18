
export type MessageType = 'info' | 'error' | 'success'

export type ServiceMessage = {
  id: string,
  timestamp: number,
  message: string,
  type: MessageType
}

export type Service = {
  name: string
  online: boolean
  messages: ServiceMessage[]
}

const mkServiceMessage = (id: string, message: string, type: MessageType): ServiceMessage => ({
  id,
  timestamp: new Date().getTime(),
  message,
  type,
})

const getOrAddService = (name: string, prev: Service[]): [number, Service[]] => {
  const idx = prev.findIndex((s) => s.name === name)
  if (idx !== -1) {
    return [idx, prev]
  }

  const newServices = [...prev, { name, online: true, messages: [] }]
  return [newServices.length - 1, newServices]
}

export const addServiceMessage = (
  serviceName: string,
  messageId: string,
  message: string,
  type: MessageType,
  online: boolean = true
) => (prev: Service[]): Service[] => {
  const [idx, newServices] = getOrAddService(serviceName, prev)

  if (newServices[idx].messages.findIndex((m) => m.id === messageId) === -1) {
    newServices[idx] = {
      ...newServices[idx],
      messages: [mkServiceMessage(messageId, message, type), ...newServices[idx].messages],
      online,
    }
  }

  return [...newServices]
}
