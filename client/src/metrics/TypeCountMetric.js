export default class TypeCountMetric {

	constructor(socket) {
		socket.addObserver(this._handleTypeCount);
	}

	_handleTypeCount = ({typeCount} = msg) => {
		let data = this._transformTypeCountToArray(typeCount);

		let max = this._findMax(data.map(d => d.count));
		let length = d3.scaleLinear().domain([0, max]).range([0, this._getMaxWidth()]);

		let colors = d3.scaleOrdinal(this._genRandomColors(10));

		let chart = d3.select("#message-type-count")
			.selectAll("div")
			.data(data, d => d.type);

		chart
			.style("margin", "2px")
			.style("font-size", "1em")
			.style("height", "48px")
			.style("line-height", "48px")
			.style("border-style", "dashed")
			.text(d => {
				return d.type + "(" +  d.count + ")";
			})
			.transition().duration(750)
			.style("width", d => length(d.count) + "px");

		chart.enter()
			.append("div")
			.style("background-color", d => colors(d.type))
			.style("width", 25)
			.transition()
			.duration(750)
			.style("width", d => length(d.count) + "px");

		chart.exit().remove();

	};

	_transformTypeCountToArray = (typeCount) => {
		let arr = [];
		for (let key in typeCount) {
			if (typeCount.hasOwnProperty(key)) {
				arr.push({type: key, count: typeCount[key]})
			}
		}
		return arr;
	};

	_findMax = (arr) => {
		let max = 0;
		arr.forEach(i => {
			if(i > max) max = i;
		});
		return max;
	};

	_genRandomColors = (amount) => {
		let colors = [];
		for(let i = 0; i < amount; i++) {
			colors.push('#'+Math.floor(Math.random()*16777215).toString(16));
		}
		return colors;
	};

	_getMaxWidth = () => {
		return document.getElementById("message-type-count").offsetWidth * 0.9;
	}

}