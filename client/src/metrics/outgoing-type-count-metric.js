import {barChart} from './bar-chart';
import {assertIsObject} from '../assert';

export const outgoingTypeCountMetric = socket => {
  assertIsObject(socket);

  const chart = barChart(document.getElementById('message-type-count-outgoing'));

  const handleTypeCount = ({outgoingTypeCount: typeCount} = msg) => {
    chart.update(typeCount);
  };

  socket.addObserver(handleTypeCount);
};