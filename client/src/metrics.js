import * as d3 from "d3";

export default class Metrics {

  constructor() {
    this.averageBytes = [];
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
    this.averageBytesChart = d3.select("#average-message-size")
	  .append("svg")
	  .attr("width", 400)
	  .attr("height", 200)
	  .append("g");

    let x = d3.scaleLinear().domain([0, 400]).range([0, 400]);
	let y = d3.scaleLinear().domain([0, 50000]).range([0, 200]);

    this.averageBytesChart.selectAll("line.x")
	  .data(x.ticks(10))
	  .enter().append("line")
	  .attr("x1", x)
	  .attr("x2", x)
	  .attr("y1", 0)
	  .attr("y2", 200)
	  .style("stroke", "#ccc");

	this.averageBytesChart.selectAll("line.y")
	  .data(y.ticks(10))
	  .enter().append("line")
	  .attr("x1", 0)
	  .attr("x2", 400)
	  .attr("y1", y)
	  .attr("y2", y)
	  .style("stroke", "#ccc");
  };

  _handleMessage = (msg) => {
    if(msg.type === "METRICS") {
		this._handleMetrics(msg);
	}
  };

  _handleMetrics = (msg) => {
    this._displayBytesSent(msg.totalBytesSent);
    this._displayAverageBytesChart(msg.averageBytesPerMessage);
  };

  _displayBytesSent = (bytesSent) => {

    let bytes = d3.select("#total-bytes-sent")
	  .selectAll("span")
	  .data([bytesSent], data => data);

	bytes.exit().remove();

    bytes.enter().append("span").text(data => "Total bytes sent: " + data);

  };

  _displayAverageBytesChart = (averageBytes) => {
    this.averageBytes.push({kb: averageBytes, time: new Date()});

    let max = d3.max(this.averageBytes.map(d => d.bytes)) * 1.25;

	let x = d3.scaleLinear().domain([0, 400]).range([0, 400]);
	let y = d3.scaleLinear().domain([0, max]).range([0, 200]);

	let i = 0;

	let line = d3.line()
	  .x(d => x(i += 5))
	  .y(d => y(max - d.bytes));

	this.averageBytesChart.exit().remove();

	this.averageBytesChart.selectAll("path")
	  .data(this.averageBytes, data => data.bytes)
	  .enter()
	  .append("path")
	  .attr("d", line(this.averageBytes))
	  .style("stroke", "#ddaddd")
	  .style("fill", "transparent");

  }

}

new Metrics();