import * as d3 from "d3";

/**
 * Encapsulates default, but customizable behavior for a live
 * d3 line chart. By default, y axis is linear, but x axis is time scale.
 */
export default class LineChart {

	constructor(domElement, width = 500, height = 300) {
		this._domElement = domElement;
		this._width = width;
		this._height = height;
		this._initialized = false;
	}

	_init = () => {
		if(this._initialized) {
			return;
		}
		this._chart = d3.select(this._domElement)
			.append("svg")
			.attr("width", this._width)
			.attr("height", this._height)
			.append("g")
			.attr("transform", "translate(" + 50 + "," + 20 + ")");

		let x = d3.scaleLinear().domain([0, this._width]).range([0, this._width]);
		let y = d3.scaleLinear().domain([0, this._height]).range([0, this._height]);

		this._chart.selectAll("line.x")
			.data(x.ticks(8))
			.enter().append("line")
			.attr("x1", x)
			.attr("x2", x)
			.attr("y1", 0)
			.attr("y2", this._height)
			.style("stroke", "#ccc");

		this._chart.selectAll("line.y")
			.data(y.ticks(6))
			.enter().append("line")
			.attr("x1", 0)
			.attr("x2", this._width)
			.attr("y1", y)
			.attr("y2", y)
			.style("stroke", "#ccc");

		let yAxis = d3.axisLeft().scale(y);
		this._chart
			.append("g")
			.attr("class", "y axis")
			.call(yAxis);

		let xAxis = d3.axisTop().scale(x);
		this._chart
			.append("g")
			.attr("class", "x axis")
			.attr("transform", "translate(0, " + 399 + ")")
			.call(xAxis);

		this._initialized = true;
	};

	/**
	 * Updates the chart with given data.
	 * Data should be an array of count-time pairs.
	 * @param data
	 * @param timeDomainMin
	 * @param timeDomainMax
	 */
	update = (data, timeDomainMin, timeDomainMax) => {
		this._init();
		let yMax = d3.max(data.map(item => item.count)) * 1.25;

		let x = d3.scaleTime().domain([timeDomainMin, timeDomainMax]).range([0, this._width]);
		let y = d3.scaleLinear().domain([0, yMax]).range([this._height, 0]);

		let line = d3.line()
			.x(d => x(d.time))
			.y(d => y(d.count));

		let path = this._chart.selectAll("path.content")
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
		this._chart.selectAll("g.y.axis")
			.call(yAxis);

		let xAxis = d3.axisTop().scale(x).ticks(4);
		this._chart.selectAll("g.x.axis")
			.call(xAxis);

	};

}