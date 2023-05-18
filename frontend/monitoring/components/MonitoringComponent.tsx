import * as React from 'react'
import Button from '@mui/material/Button'
import ButtonGroup from '@mui/material/ButtonGroup'
import Grid from '@mui/material/Grid'

import { useServices } from '../ServicesContext'

import { Messages } from './Messages'

export const MonitoringComponent: React.FC = () => {
  const [selectedItem, setSelectedItem] = React.useState<number>(0);
  const handleServiceClick = (item: number) => () => setSelectedItem(item)
  const services = useServices()

  return (
    <Grid container spacing={4}>
      <Grid item xs={12} md={2}>
        <ButtonGroup fullWidth orientation="vertical">
          {services.map((service, index) =>
            <Button
              key={service.name}
              variant={selectedItem === index ? "contained" : "outlined"}
              onClick={handleServiceClick(index)}
            >
              {service.name}
            </Button>
          )}
        </ButtonGroup>
      </Grid>
      <Grid item xs={12} md={10} xl={8}>
        {services.length > 0 && (
          <Messages messages={services[selectedItem].messages} />
        )}
      </Grid>
    </Grid>
  );
}
