import * as d3 from "d3";

/**
 * Encapsulates default, but customizable behavior for a live
 * d3 line chart.
 */
export default class LineChart {

	constructor(domElement) {
		this._domElement = domElement;
	}

	width = (width) => {
		this._width = width;
		return this;
	};

	height = (height) => {
		this._height = height;
		return this;
	};

	init = () => {
		this.averageKbsChart = d3.select(this._domElement)
			.append("svg")
			.attr("width", this._width)
			.attr("height", this._height)
			.append("g")
			.attr("transform", "translate(" + 50 + "," + 20 + ")");

		let x = d3.scaleLinear().domain([0, this._width]).range([0, this._width]);
		let y = d3.scaleLinear().domain([0, this._height]).range([0, this._height]);

		this.averageKbsChart.selectAll("line.x")
			.data(x.ticks(8))
			.enter().append("line")
			.attr("x1", x)
			.attr("x2", x)
			.attr("y1", 0)
			.attr("y2", this._height)
			.style("stroke", "#ccc");

		this.averageKbsChart.selectAll("line.y")
			.data(y.ticks(6))
			.enter().append("line")
			.attr("x1", 0)
			.attr("x2", this._width)
			.attr("y1", y)
			.attr("y2", y)
			.style("stroke", "#ccc");

		let yAxis = d3.axisLeft().scale(y);
		this.averageKbsChart
			.append("g")
			.attr("class", "y axis")
			.call(yAxis);

		let xAxis = d3.axisTop().scale(x);
		this.averageKbsChart
			.append("g")
			.attr("class", "x axis")
			.attr("transform", "translate(0, " + 399 + ")")
			.call(xAxis);

		return this;
	};

	update = (data, timeDomainMin, timeDomainMax) => {
		let yMax = d3.max(data.map(item => item.count)) * 1.25;

		let x = d3.scaleTime().domain([timeDomainMin, timeDomainMax]).range([0, this._width]);
		let y = d3.scaleLinear().domain([0, yMax]).range([this._height, 0]);

		let line = d3.line()
			.x(d => x(d.time))
			.y(d => y(d.count));

		let path = this.averageKbsChart.selectAll("path.content")
			.data([data]);

		path.exit().remove();

		path.enter()
			.append("path")
			.attr("class", "content")
			.style("stroke", "#000000")
			.style("stroke-width", "2px")
			.style("fill", "transparent");

		path.attr("d", line(data));

		let yAxis = d3.axisLeft().scale(y);
		this.averageKbsChart.selectAll("g.y.axis")
			.call(yAxis);

		let xAxis = d3.axisTop().scale(x).ticks(4);
		this.averageKbsChart.selectAll("g.x.axis")
			.call(xAxis);

	};

}