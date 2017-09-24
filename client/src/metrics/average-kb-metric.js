import {lineChart} from './line-chart';
import {textD3} from './text-d3';
import {getNumberOfProperties, throwError} from '../utils';
import {assertIsArray, assertIsNumber, assertIsObject, assertIsString} from '../assert';

const outgoingTextFunction = data => {
	return `Average outgoing ${data} kb/s`;
};

const incomingTextFunction = data => {
	return `Average incoming ${data} kb/s`;
};

const getTimeDomainMax = () => {
	const date = new Date();
	date.setMinutes(date.getMinutes() + 1);
	return date;
};

const handleResponse = response => {
	if (response.status !== 200) {
		throwError(`ERROR! Received response with status: ${response.status}`);
	}
	return response.json();
};

/**
 * Removes all properties of data object which are not a number.
 * @param data
 */
const sanitizeData = data => {
	assertIsObject(data);
	for (const timestamp in data) {
		if (isNaN(data[timestamp])) {
			delete data[timestamp];
		}
	}
};

/**
 * If data has less properties than maxElements, does not modify data.
 * If data has more properties than maxElements, tries to remove properties
 * to make it have less properties than maxElements. Properties
 * should be removed uniformly.
 * @param data
 * @param maxElements
 */
const thinData = (data, maxElements) => {
	assertIsObject(data);
	assertIsNumber(maxElements);

	const elements = getNumberOfProperties(data);
	if (elements > maxElements) {
		const leaveNthElement = Math.ceil(elements / maxElements);
		let index = 0;
		for (const timestamp in data) {
			if (index % leaveNthElement !== 0) {
				delete data[timestamp];
			}
			index++;
		}
		if (getNumberOfProperties(data) > maxElements) {
			thinData(data, maxElements);
		}
	}
};

const transformData = averageKbs => {
	assertIsArray(averageKbs);
	return averageKbs.map(item => {
		return {count: item.kbs, time: item.time};
	});
};

const getTimeDomainMin = (mode, days) => {
	assertIsString(mode);
	const date = new Date();
	if (mode === 'live') {
		date.setMinutes(date.getMinutes() - 4);
		return date;
	}
	if (mode === 'days') {
		date.setMinutes(0);
		date.setHours(0);
		date.setSeconds(0);
		assertIsNumber(days);
		if (days === 1) {
			return date;
		}
		date.setDate(date.getDate() - days);
		return date;
	}
};

export const averageKbMetric = socket => {
	assertIsObject(socket);

	const chartOutgoing = lineChart(document.getElementById('average-kbs-outgoing'), 850, 420);
	const chartIncoming = lineChart(document.getElementById('average-kbs-incoming'), 850, 420);
	const textOutgoing = textD3(document.getElementById('average-kbs-outgoing'), outgoingTextFunction);
	const textIncoming = textD3(document.getElementById('average-kbs-incoming'), incomingTextFunction);
	const maxElements = 500;

	let mode = 'live';
	let days = 1;
	let averageOutgoingKbs = [];
	let averageIncomingKbs = [];

	const displayOutgoing = data => {
		sanitizeData(data);
		thinData(data, maxElements);

		averageOutgoingKbs = [];
		for (const timestamp in data) {
			addAverageOutgoingKbsToDataSet(data[timestamp], new Date(parseInt(timestamp)));
		}

		averageOutgoingKbs.sort((a, b) => a.time.getTime() - b.time.getTime());

		const transformedData = transformData(averageOutgoingKbs);
		const timeDomainMin = getTimeDomainMin(mode, days);
		const timeDomainMax = getTimeDomainMax();
		chartOutgoing.update(transformedData, timeDomainMin, timeDomainMax);
	};

	const handleAverageIncomingKbs = ({averageIncomingKbs: averageKbs} = msg) => {
		addAverageIncomingKbsToDataSet(averageKbs);
		const transformedData = transformData(averageIncomingKbs);
		if (transformedData.length === 0) return;
		textIncoming.update(transformedData[transformedData.length - 1].count);

		if (mode !== 'live') {
			return;
		}

		const timeDomainMin = getTimeDomainMin(mode, days);
		const timeDomainMax = getTimeDomainMax();
		chartIncoming.update(transformedData, timeDomainMin, timeDomainMax);
	};

	socket.addObserver(handleAverageIncomingKbs);

	const addAverageIncomingKbsToDataSet = (averageKbs, time) => {
		averageIncomingKbs.push({kbs: averageKbs, time: time ? time : new Date()});
		const timeDomainMin = getTimeDomainMin(mode, days);
		averageIncomingKbs = averageIncomingKbs.filter(item => {
			return item.time > timeDomainMin;
		});
	};

	const displayIncoming = data => {
		sanitizeData(data);
		thinData(data);

		averageIncomingKbs = [];
		for (const timestamp in data) {
			addAverageIncomingKbsToDataSet(data[timestamp], new Date(parseInt(timestamp)));
		}

		averageIncomingKbs.sort((a, b) => a.time.getTime() - b.time.getTime());

		const transformedData = transformData(averageIncomingKbs);
		const timeDomainMin = getTimeDomainMin(mode, days);
		const timeDomainMax = getTimeDomainMax();
		chartIncoming.update(transformedData, timeDomainMin, timeDomainMax);
	};

	const addAverageOutgoingKbsToDataSet = (averageKbs, time) => {
		if (isNaN(averageKbs)) {
			return;
		}
		averageOutgoingKbs.push({kbs: averageKbs, time: time ? time : new Date()});
		const timeDomainMin = getTimeDomainMin(mode, days);
		averageOutgoingKbs = averageOutgoingKbs.filter(item => {
			return item.time > timeDomainMin;
		});
	};

	const handleAverageOutgoingKbs = ({averageOutgoingKbs: averageKbs} = msg) => {
		addAverageOutgoingKbsToDataSet(averageKbs);
		const transformedData = transformData(averageOutgoingKbs);
		textOutgoing.update(transformedData[transformedData.length - 1].count);

		if (mode !== 'live') {
			return;
		}

		const timeDomainMin = getTimeDomainMin(mode, days);
		const timeDomainMax = getTimeDomainMax();
		chartOutgoing.update(transformedData, timeDomainMin, timeDomainMax);
	};

	socket.addObserver(handleAverageOutgoingKbs);

	const averageKbMetric = {};

	averageKbMetric.setLive = () => {
		mode = 'live';
	};

	averageKbMetric.setDays = d => {
		assertIsNumber(d);
		mode = 'days';
		days = d;
		fetch('metrics/outgoing?days=' + days)
			.then(handleResponse)
			.then(displayOutgoing);

		fetch('metrics/incoming?days=' + days)
			.then(handleResponse)
			.then(displayIncoming);
	};

	return averageKbMetric;
};