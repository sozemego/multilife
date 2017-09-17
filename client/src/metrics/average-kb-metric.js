import {lineChart} from "./line-chart";
import {textD3} from "./text-d3";

const outgoingTextFunction = (data) => {
	return "Average outgoing " + data + " kb/s";
};

const incomingTextFunction = (data) => {
	return "Average incoming " + data + " kb/s";
};

const getTimeDomainMax = () => {
	let date = new Date();
	date.setMinutes(date.getMinutes() + 1);
	return date;
};

const handleResponse = response => {
	if(response.status !== 200) {
		throw new Error("Received response with status: " + response.status);
	}
	return response.json();
};

const sanitizeData = data => {
	for(let timestamp in data) {
		if(isNaN(data[timestamp])) {
			delete data[timestamp];
		}
	}
};

const thinData = (data, maxElements) => {
	const elements = Object.keys(data).length;
	if(elements > maxElements) {
		const leaveNthElement = Math.ceil(elements / maxElements);
		let index = 0;
		for(let timestamp in data) {
			if(index % leaveNthElement !== 0) {
				delete data[timestamp];
			}
			index++;
		}
	}
};

export const averageKbMetric = socket => {

	let averageOutgoingKbs = [];
	let averageIncomingKbs = [];
	const chartOutgoing = lineChart(document.getElementById("average-kbs-outgoing"), 850, 420);
	const chartIncoming = lineChart(document.getElementById("average-kbs-incoming"), 850, 420);
	const textOutgoing = textD3(document.getElementById("average-kbs-outgoing"),  outgoingTextFunction);
	const textIncoming = textD3(document.getElementById("average-kbs-incoming"),  incomingTextFunction);
	let mode = "live";
	let days = 1;
	const maxElements = 500;

	const displayOutgoing = data => {
		sanitizeData(data);
		thinData(data, maxElements);

		averageOutgoingKbs = [];
		for(let timestamp in data) {
			addAverageOutgoingKbsToDataSet(data[timestamp], new Date(parseInt(timestamp)));
		}

		averageOutgoingKbs.sort((a, b) => a.time.getTime() - b.time.getTime());

		let transformedData = transformOutgoingData();
		let timeDomainMin = getTimeDomainMin();
		let timeDomainMax = getTimeDomainMax();
		chartOutgoing.update(transformedData, timeDomainMin, timeDomainMax);
	};

	const handleAverageIncomingKbs = ({averageIncomingKbs : averageKbs} = msg) => {
		addAverageIncomingKbsToDataSet(averageKbs);
		let transformedData = transformIncomingData();
		if(transformedData.length === 0) return;
		textIncoming.update(transformedData[transformedData.length - 1].count);

		if (mode !== "live") {
			return;
		}

		let timeDomainMin = getTimeDomainMin();
		let timeDomainMax = getTimeDomainMax();
		chartIncoming.update(transformedData, timeDomainMin, timeDomainMax);
	};

	socket.addObserver(handleAverageIncomingKbs);

	const addAverageIncomingKbsToDataSet = (averageKbs, time) => {
		averageIncomingKbs.push({kbs: averageKbs, time: time ? time: new Date()});
		let timeDomainMin = getTimeDomainMin();
		averageIncomingKbs = averageIncomingKbs.filter(item => {
			return item.time > timeDomainMin;
		});
	};

	const transformIncomingData = () => {
		return averageIncomingKbs.map(item => {
			return {count: item.kbs, time: item.time};
		})
	};

	const displayIncoming = (data) => {
		sanitizeData(data);
		thinData(data);

		averageIncomingKbs = [];
		for(let timestamp in data) {
			addAverageIncomingKbsToDataSet(data[timestamp], new Date(parseInt(timestamp)));
		}

		averageIncomingKbs.sort((a, b) => a.time.getTime() - b.time.getTime());

		let transformedData = transformIncomingData();
		let timeDomainMin = getTimeDomainMin();
		let timeDomainMax = getTimeDomainMax();
		chartIncoming.update(transformedData, timeDomainMin, timeDomainMax);
	};

	const getTimeDomainMin = () => {
		let date = new Date();
		if(mode === "live") {
			date.setMinutes(date.getMinutes() - 4);
			return date;
		}
		if(mode === "days") {
			date.setMinutes(0);
			date.setHours(0);
			date.setSeconds(0);
			if(days === 1) {
				return date;
			}
			date.setDate(date.getDate() - days);
			return date;
		}
	};

	const addAverageOutgoingKbsToDataSet = (averageKbs, time) => {
		if(isNaN(averageKbs)) {
			return;
		}
		averageOutgoingKbs.push({kbs: averageKbs, time: time ? time : new Date()});
		let timeDomainMin = getTimeDomainMin();
		averageOutgoingKbs = averageOutgoingKbs.filter(item => {
			return item.time > timeDomainMin;
		});
	};

	const transformOutgoingData = () => {
		return averageOutgoingKbs.map(item => {
			return {count: item.kbs, time: item.time};
		})
	};

	const handleAverageOutgoingKbs = ({averageOutgoingKbs : averageKbs} = msg) => {
		addAverageOutgoingKbsToDataSet(averageKbs);
		let transformedData = transformOutgoingData();
		textOutgoing.update(transformedData[transformedData.length - 1].count);

		if (mode !== "live") {
			return;
		}

		let timeDomainMin = getTimeDomainMin();
		let timeDomainMax = getTimeDomainMax();
		chartOutgoing.update(transformedData, timeDomainMin, timeDomainMax);
	};

	socket.addObserver(handleAverageOutgoingKbs);

	const averageKbMetric = {};

	averageKbMetric.setLive = () => {
		mode = "live";
	};

	averageKbMetric.setDays = d => {
		if(!days instanceof Number) {
			throw new Error("Days have to be a number.");
		}
		mode = "days";
		days = d;
		fetch("metrics/outgoing?days=" + days)
			.then(handleResponse)
			.then(displayOutgoing);

		fetch("metrics/incoming?days=" + days)
			.then(handleResponse)
			.then(displayIncoming);
	};

	return averageKbMetric;
};