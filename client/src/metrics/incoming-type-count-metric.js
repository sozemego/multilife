import {barChart} from './bar-chart';
import {assertIsObject} from '../assert';

export const incomingTypeCountMetric = socket => {
	assertIsObject(socket);

	const chart = barChart(document.getElementById('message-type-count-incoming'));

	const handleTypeCount = ({incomingTypeCount: typeCount} = msg) => {
		chart.update(typeCount);
	};

	socket.addObserver(handleTypeCount);
};