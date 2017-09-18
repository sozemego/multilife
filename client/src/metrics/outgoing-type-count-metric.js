import {barChart} from "./bar-chart";

export const outgoingTypeCountMetric = socket => {

	const chart = barChart(document.getElementById("message-type-count-outgoing"));

	const handleTypeCount = ({outgoingTypeCount: typeCount} = msg) => {
		chart.update(typeCount);
	};

	socket.addObserver(handleTypeCount);
};