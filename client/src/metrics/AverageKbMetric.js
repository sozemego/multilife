import * as d3 from "d3";

export default class AverageKbMetric {

	constructor(socket) {
		socket.addObserver(this._handleAverageKbs);
		this.averageKbs = [];
		this._init();
	}

	_init = () => {
		this.kbsChartWidth = 800;
		this.kbsChartHeight = 420;
		this.averageKbsChart = d3.select("#average-kbs")
			.append("svg")
			.attr("width", this.kbsChartWidth)
			.attr("height", this.kbsChartHeight)
			.append("g")
			.attr("transform", "translate(" + 100 + "," + 20 + ")");

		let x = d3.scaleLinear().domain([0, this.kbsChartWidth]).range([0, this.kbsChartWidth]);
		let y = d3.scaleLinear().domain([0, this.kbsChartHeight]).range([0, this.kbsChartHeight]);

		this.averageKbsChart.selectAll("line.x")
			.data(x.ticks(7))
			.enter().append("line")
			.attr("x1", x)
			.attr("x2", x)
			.attr("y1", 0)
			.attr("y2", this.kbsChartHeight)
			.style("stroke", "#ccc");

		this.averageKbsChart.selectAll("line.y")
			.data(y.ticks(6))
			.enter().append("line")
			.attr("x1", 0)
			.attr("x2", this.kbsChartWidth)
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
	};

	_handleAverageKbs = ({averageKbs} = msg) => {
		this._addAverageKbsToDataSet(averageKbs);

		let max = d3.max(this.averageKbs.map(d => d.kbs)) * 1.25;

		let timeDomainMin = this._getTimeDomainMin();
		let timeDomainMax = this._getTimeDomainMax();

		let x = d3.scaleTime().domain([timeDomainMin, timeDomainMax]).range([0, this.kbsChartWidth]);
		let y = d3.scaleLinear().domain([0, max]).range([this.kbsChartHeight, 0]);

		let line = d3.line()
			.x(d => x(d.time))
			.y(d => y(d.kbs));

		let path = this.averageKbsChart.selectAll("path.content")
			.data([this.averageKbs]);

		path.exit().remove();

		path.enter()
			.append("path")
			.attr("class", "content")
			.style("stroke", "#000000")
			.style("stroke-width", "2px")
			.style("fill", "transparent");

		path.attr("d", line(this.averageKbs));

		let yAxis = d3.axisLeft().scale(y);
		this.averageKbsChart.selectAll("g.y.axis")
			.call(yAxis);

		let xAxis = d3.axisTop().scale(x).ticks(4);
		this.averageKbsChart.selectAll("g.x.axis")
			.call(xAxis);

	};

	_addAverageKbsToDataSet = (averageKbs) => {
		this.averageKbs.push({kbs: averageKbs, time: new Date()});
		let timeDomainMin = this._getTimeDomainMin();
		this.averageKbs = this.averageKbs.filter(item => {
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

}