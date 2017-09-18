import * as d3 from "d3";

/**
 * Encapsulates default, but customizable behavior for a live
 * d3 line chart. By default, y axis is linear, but x axis is time scale.
 */
export const lineChart = (dom, width, height) => {

	let initialized = false;
	let chart = null;

	const lineChart = {};

	lineChart.init = () => {
		if (initialized) {
			return;
		}
		chart = d3.select(dom)
			.append("svg")
			.attr("width", width)
			.attr("height", height)
			.append("g")
			.attr("transform", "translate(" + 50 + "," + 20 + ")");

		const x = d3.scaleLinear().domain([0, width]).range([0, width]);
		const y = d3.scaleLinear().domain([0, height]).range([0, height]);

		chart.selectAll("line.x")
			.data(x.ticks(8))
			.enter().append("line")
			.attr("x1", x)
			.attr("x2", x)
			.attr("y1", 0)
			.attr("y2", height)
			.style("stroke", "#ccc");

		chart.selectAll("line.y")
			.data(y.ticks(6))
			.enter().append("line")
			.attr("x1", 0)
			.attr("x2", width)
			.attr("y1", y)
			.attr("y2", y)
			.style("stroke", "#ccc");

		const yAxis = d3.axisLeft().scale(y);
		chart
			.append("g")
			.attr("class", "y axis")
			.call(yAxis);

		const xAxis = d3.axisTop().scale(x);
		chart
			.append("g")
			.attr("class", "x axis")
			.attr("transform", "translate(0, " + 399 + ")")
			.call(xAxis);

		initialized = true;
	};

	/**
	 * Updates the chart with given data.
	 * Data should be an array of count-time pairs.
	 * @param data
	 * @param timeDomainMin
	 * @param timeDomainMax
	 */
	lineChart.update = (data, timeDomainMin, timeDomainMax) => {

		lineChart.init();
		const yMax = d3.max(data.map(item => item.count)) * 1.25;

		const x = d3.scaleTime().domain([timeDomainMin, timeDomainMax]).range([0, width]);
		const y = d3.scaleLinear().domain([0, yMax]).range([height, 0]);

		const line = d3.line()
			.x(d => x(d.time))
			.y(d => y(d.count));

		const path = chart.selectAll("path.content")
			.data([data]);

		path.exit().remove();

		path.enter()
			.append("path")
			.attr("class", "content")
			.style("stroke", "#000000")
			.style("stroke-width", "2px")
			.style("fill", "transparent");

		path.attr("d", line(data));

		const yAxis = d3.axisLeft().scale(y);
		chart.selectAll("g.y.axis")
			.call(yAxis);

		const xAxis = d3.axisTop().scale(x).ticks(4);
		chart.selectAll("g.x.axis")
			.call(xAxis);

	};

	return lineChart;
};
