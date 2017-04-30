import * as d3 from "d3";

export default class MetricsController {

	constructor() {
		this.averageKbs = [];
		this._init();
	}

	_init() {
		this.webSocket = new WebSocket("ws://localhost:8080/metrics-live");

		this.webSocket.onopen = () => {
			this._initCharts();
		};

		this.webSocket.onmessage = (msg) => {
			this._handleMessage(JSON.parse(msg.data));
		};
	}

	_initCharts = () => {
		this._initAverageKbsChart();
		this._initMessageTypeCountsChart();
	};

	_initAverageKbsChart = () => {
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

	_initMessageTypeCountsChart = () => {
		//this._typeCountChart = d3.select("#message-type-count").selectAll("div");
	};

	_handleMessage = (msg) => {
		if (msg.type === "METRICS") {
			this._handleMetrics(msg);
		}
	};

	_handleMetrics = (msg) => {
		this._displaySent(msg.totalBytesSent, msg.totalMessagesSent);
		this._displayAverageKbsChart(msg.averageKbs);
		this._displayMessageTypeCounts(msg.typeCount);
	};

	_displaySent = (bytesSent, messagesSent) => {
		let bytes = d3.select("#total-bytes-sent")
			.selectAll("span")
			.data([bytesSent], data => data);

		bytes.exit().remove();

		bytes.enter().append("span").text(data => {
			return "Total bytes sent: "
				+ data + " (" + this._getTruncatedKb(data)
				+ "kB, " + this._getTruncatedMb(data) + "MB). "
				+ "Total messages: " + messagesSent;
		});

	};

	_getTruncatedKb = (bytes) => {
		return ("" + (bytes / 1024)).substr(0, 4);
	};

	_getTruncatedMb = (bytes) => {
		return ("" + ((bytes / 1024) / 1024)).substr(0, 4);
	};

	_displayAverageKbsChart = (averageKbs) => {
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

	_displayMessageTypeCounts = (typeCount) => {
		let data = this._transformTypeCountToArray(typeCount);

		let max = this._findMax(data.map(d => d.count));
		let length = d3.scaleLinear().domain([0, max]).range([0, 450]);

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

}

new Metrics();