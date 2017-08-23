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
	}

	_handleAverageOutgoingKbs = ({averageOutgoingKbs : averageKbs} = msg) => {
		this._addAverageOutgoingKbsToDataSet(averageKbs);
		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();
		let transformedData = this._transformOutgoingData();
		this._chartOutgoing.update(transformedData, timeDomainMin, timeDomainMax);
		this._textOutgoing.update(transformedData[transformedData.length - 1].count);
	};

	_handleAverageIncomingKbs = ({averageIncomingKbs : averageKbs} = msg) => {
		this._addAverageIncomingKbsToDataSet(averageKbs);
		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();
		let transformedData = this._transformIncomingData();
		this._chartIncoming.update(transformedData, timeDomainMin, timeDomainMax);
		this._textIncoming.update(transformedData[transformedData.length - 1].count);
	};

	_addAverageOutgoingKbsToDataSet = (averageKbs) => {
		this._averageOutgoingKbs.push({kbs: averageKbs, time: new Date()});
		let timeDomainMin = this._getTimeDomainMin();
		this._averageOutgoingKbs = this._averageOutgoingKbs.filter(item => {
			return item.time > timeDomainMin;
		});
	};

	_addAverageIncomingKbsToDataSet = (averageKbs) => {
		this._averageIncomingKbs.push({kbs: averageKbs, time: new Date()});
		let timeDomainMin = this._getTimeDomainMin();
		this._averageIncomingKbs = this._averageIncomingKbs.filter(item => {
			return item.time > timeDomainMin;
		});
	};

	_getTimeDomainMin = () => {
		let date = new Date();
		date.setMinutes(date.getMinutes() - 4);
		return date;
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