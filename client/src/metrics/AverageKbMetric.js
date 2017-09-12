import LineChart from "./LineChart";
import TextD3 from "./TextD3";

export default class AverageKbMetric {

	constructor(socket) {
		socket.addObserver(this._handleAverageOutgoingKbs);
		socket.addObserver(this._handleAverageIncomingKbs);
		this._averageOutgoingKbs = [];
		this._averageIncomingKbs = [];
		this._chartOutgoing = new LineChart(document.getElementById("average-kbs-outgoing"), 850, 420);
		this._chartIncoming = new LineChart(document.getElementById("average-kbs-incoming"), 850, 420);
		this._textOutgoing = new TextD3(document.getElementById("average-kbs-outgoing"), this._outgoingTextFunction);
		this._textIncoming = new TextD3(document.getElementById("average-kbs-incoming"), this._incomingTextFunction);
		this._mode = "live";
		this._days = 1;
		this._maxElements = 500;
	}

	setLive = () => {
		this._mode = "live";
	};

	setDays = (days) => {
		if(!days instanceof Number) {
			throw new Error("Days have to be a number.");
		}
		this._mode = "days";
		this._days = days;
		fetch("metrics/outgoing?days=" + days)
			.then(this._handleResponse)
			.then(this._displayOutgoing);

		fetch("metrics/incoming?days=" + days)
			.then(this._handleResponse)
			.then(this._displayIncoming);
	};

	_handleResponse = (response) => {
		if(response.status !== 200) {
			throw new Error("Received response with status: " + response.status);
		}
		return response.json();
	};

	_displayOutgoing = (data) => {
		this._thinData(data);

		this._averageOutgoingKbs = [];
		for(let timestamp in data) {
			this._addAverageOutgoingKbsToDataSet(data[timestamp], new Date(parseInt(timestamp)));
		}

		this._averageOutgoingKbs.sort((a, b) => a.time.getTime() - b.time.getTime());

		let transformedData = this._transformOutgoingData();
		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();
		this._chartOutgoing.update(transformedData, timeDomainMin, timeDomainMax);
	};

	_displayIncoming = (data) => {
		this._thinData(data);

		this._averageIncomingKbs = [];
		for(let timestamp in data) {
			this._addAverageIncomingKbsToDataSet(data[timestamp], new Date(parseInt(timestamp)));
		}

		this._averageIncomingKbs.sort((a, b) => a.time.getTime() - b.time.getTime());

		let transformedData = this._transformIncomingData();
		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();
		this._chartIncoming.update(transformedData, timeDomainMin, timeDomainMax);
	};

	_thinData = (data) => {
		const elements = Object.keys(data).length;
		if(elements > this._maxElements) {
			const leaveNthElement = Math.ceil(elements / this._maxElements);
			let index = 0;
			for(let timestamp in data) {
				if(index % leaveNthElement !== 0) {
					delete data[timestamp];
				}
				index++;
			}
		}
	};

	_handleAverageOutgoingKbs = ({averageOutgoingKbs : averageKbs} = msg) => {
		this._addAverageOutgoingKbsToDataSet(averageKbs);
		let transformedData = this._transformOutgoingData();
		this._textOutgoing.update(transformedData[transformedData.length - 1].count);

		if (this._mode !== "live") {
			return;
		}

		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();
		this._chartOutgoing.update(transformedData, timeDomainMin, timeDomainMax);
	};

	_handleAverageIncomingKbs = ({averageIncomingKbs : averageKbs} = msg) => {
		this._addAverageIncomingKbsToDataSet(averageKbs);
		let transformedData = this._transformIncomingData();
		if(transformedData.length === 0) return;
		this._textIncoming.update(transformedData[transformedData.length - 1].count);

		if (this._mode !== "live") {
			return;
		}

		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();
		this._chartIncoming.update(transformedData, timeDomainMin, timeDomainMax);
	};

	_addAverageOutgoingKbsToDataSet = (averageKbs, time) => {
		if(isNaN(averageKbs)) {
			return;
		}
		this._averageOutgoingKbs.push({kbs: averageKbs, time: time ? time : new Date()});
		let timeDomainMin = this._getTimeDomainMin();
		this._averageOutgoingKbs = this._averageOutgoingKbs.filter(item => {
			return item.time > timeDomainMin;
		});
	};

	_addAverageIncomingKbsToDataSet = (averageKbs, time) => {
		this._averageIncomingKbs.push({kbs: averageKbs, time: time ? time: new Date()});
		let timeDomainMin = this._getTimeDomainMin();
		this._averageIncomingKbs = this._averageIncomingKbs.filter(item => {
			return item.time > timeDomainMin;
		});
	};

	_getTimeDomainMin = () => {
		let date = new Date();
		if(this._mode === "live") {
			date.setMinutes(date.getMinutes() - 4);
			return date;
		}
		if(this._mode === "days") {
			date.setMinutes(0);
			date.setHours(0);
			date.setSeconds(0);
			if(this._days === 1) {
				return date;
			}
			date.setDate(date.getDate() - this._days);
			return date;
		}
	};

	_getTimeDomainMax = () => {
		let date = new Date();
		date.setMinutes(date.getMinutes() + 1);
		return date;
	};

	_outgoingTextFunction = (data) => {
		return "Average outgoing " + data + " kb/s";
	};

	_incomingTextFunction = (data) => {
		return "Average incoming " + data + " kb/s";
	};

	_transformOutgoingData = () => {
		return this._averageOutgoingKbs.map(item => {
			return {count: item.kbs, time: item.time};
		})
	};

	_transformIncomingData = () => {
		return this._averageIncomingKbs.map(item => {
			return {count: item.kbs, time: item.time};
		})
	};

}