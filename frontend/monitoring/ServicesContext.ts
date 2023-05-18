import React from 'react'

import { Service } from './services'

export const ServicesContext = React.createContext<Service[]>([])
export const useServices = () => React.useContext(ServicesContext)
