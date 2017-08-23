import LineChart from "./LineChart";
import TextD3 from "./TextD3";

export default class AverageKbMetric {

	constructor(socket) {
		socket.addObserver(this._handleAverageKbs);
		this._averageKbs = [];
		this._chart = new LineChart(document.getElementById("average-kbs"), 850, 420);
		this._text = new TextD3(document.getElementById("average-kbs"), this._textFunction);
	}

	_handleAverageKbs = ({averageOutgoingKbs : averageKbs} = msg) => {
		this._addAverageKbsToDataSet(averageKbs);
		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();
		let transformedData = this._transformData();
		this._chart.update(transformedData, timeDomainMin, timeDomainMax);
		this._text.update(transformedData[transformedData.length - 1].count);
	};

	_addAverageKbsToDataSet = (averageKbs) => {
		this._averageKbs.push({kbs: averageKbs, time: new Date()});
		let timeDomainMin = this._getTimeDomainMin();
		this._averageKbs = this._averageKbs.filter(item => {
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

	_textFunction = (data) => {
		return "Average outgoing " + data + " kb/s";
	};

	_transformData = () => {
		return this._averageKbs.map(item => {
			return {count: item.kbs, time: item.time};
		})
	}

}