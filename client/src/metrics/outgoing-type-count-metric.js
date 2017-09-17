import {findMax, generateRandomColors} from "./utils";

const transformTypeCountToArray = (typeCount) => {
	const arr = [];
	for (let key in typeCount) {
		if (typeCount.hasOwnProperty(key)) {
			arr.push({type: key, count: typeCount[key]})
		}
	}
	return arr;
};

const getMaxWidth = () => {
	return document.getElementById("message-type-count-outgoing").offsetWidth * 0.9;
};

export const outgoingTypeCountMetric = socket => {

	const handleTypeCount = ({outgoingTypeCount: typeCount} = msg) => {
		let data = transformTypeCountToArray(typeCount);

		let max = findMax(data.map(d => d.count));
		let length = d3.scaleLinear().domain([0, max]).range([0, getMaxWidth()]);

		let colors = d3.scaleOrdinal(generateRandomColors(10));

		let chart = d3.select("#message-type-count-outgoing")
			.selectAll("div")
			.data(data, d => d.type);

		chart
			.style("margin", "2px")
			.style("font-size", "1em")
			.style("height", "40px")
			.style("line-height", "40px")
			.style("border-style", "solid")
			.text(d => {
				return d.type + "(" +  d.count + ")";
			})
			.transition()
			.duration(500)
			.style("width", d => length(d.count) + "px");

		chart.enter()
			.append("div")
			.style("background-color", d => colors(d.type))
			.style("width", 25)
			.transition()
			.duration(500)
			.style("width", d => length(d.count) + "px");

		chart.exit().remove();
	};

	socket.addObserver(handleTypeCount);
};