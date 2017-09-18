import {lineChart} from "./line-chart";
import {textD3} from "./text-d3";

const textFunction = (data) => {
	return "Currently " + data + " players playing.";
};

const getTimeDomainMin = () => {
	const date = new Date();
	date.setMinutes(date.getMinutes() - 4);
	return date;
};

const getTimeDomainMax = () => {
	const date = new Date();
	date.setMinutes(date.getMinutes() + 1);
	return date;
};

export const playerCountMetric = socket => {

	const playerCount = [];
	const chart = lineChart(document.getElementById("player-count"), 850, 420);
	const text = textD3(document.getElementById("player-count"), textFunction);

	const addPlayerCount = instancePlayerMap => {
		playerCount.push({players: Object.keys(instancePlayerMap).length, time: new Date()});
	};

	const transformData = () => {
		return playerCount.map(item => {
			return {count: item.players, time: item.time};
		})
	};

	const handlePlayerMapCount = ({instancePlayerMap} = msg) => {
		addPlayerCount(instancePlayerMap);
		const timeDomainMin = getTimeDomainMin();
		const timeDomainMax = getTimeDomainMax();
		const transformedData = transformData();
		chart.update(transformedData, timeDomainMin, timeDomainMax);
		text.update(transformedData[transformedData.length - 1].count);
	};

	socket.addObserver(handlePlayerMapCount);
};