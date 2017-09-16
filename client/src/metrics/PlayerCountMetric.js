import {lineChart} from "./line-chart";
import {textD3} from "./text-d3";
export default class PlayerCountMetric {

	constructor(socket) {
		socket.addObserver(this._handlePlayerMapCount);
		this._playerCount = [];
		this._chart = lineChart(document.getElementById("player-count"), 850, 420);
		this._text = textD3(document.getElementById("player-count"), this._textFunction);
	}

	_handlePlayerMapCount = ({instancePlayerMap} = msg) => {
		this._addPlayerCount(instancePlayerMap);
		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();
		let transformedData = this._transformData();
		this._chart.update(transformedData, timeDomainMin, timeDomainMax);
		this._text.update(transformedData[transformedData.length - 1].count);
	};

	_addPlayerCount = (instancePlayerMap) => {
		this._playerCount.push({players: Object.keys(instancePlayerMap).length, time: new Date()});
	};

	_textFunction = (data) => {
		return "Currently " + data + " players playing.";
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

	_transformData = () => {
		return this._playerCount.map(item => {
			return {count: item.players, time: item.time};
		})
	}

}