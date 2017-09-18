import {barChart} from "./bar-chart";

export const incomingTypeCountMetric = socket => {

	const chart = barChart(document.getElementById("message-type-count-incoming"));

	const handleTypeCount = ({incomingTypeCount: typeCount} = msg) => {
		chart.update(typeCount);
	};

	socket.addObserver(handleTypeCount);
};