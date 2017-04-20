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
	this._initAverageBytesChart();
	this._initMessageTypeCountsChart();
  };

  _initAverageBytesChart = () => {
	this.averageBytesChart = d3.select("#average-message-size")
	  .append("svg")
	  .attr("width", 630)
	  .attr("height", 420)
	  .append("g")
	  .attr("transform", "translate(30,20)");

	let x = d3.scaleLinear().domain([0, 600]).range([0, 600]);
	let y = d3.scaleLinear().domain([0, 50000]).range([0, 400]);

	this.averageBytesChart.selectAll("line.x")
	  .data(x.ticks(5))
	  .enter().append("line")
	  .attr("x1", x)
	  .attr("x2", x)
	  .attr("y1", 0)
	  .attr("y2", 400)
	  .style("stroke", "#ccc");

	this.averageBytesChart.selectAll("line.y")
	  .data(y.ticks(5))
	  .enter().append("line")
	  .attr("x1", 0)
	  .attr("x2", 600)
	  .attr("y1", y)
	  .attr("y2", y)
	  .style("stroke", "#ccc");

	let yAxis = d3.axisLeft().scale(y);
	this.averageBytesChart
	  .append("g")
	  .attr("class", "y axis")
	  .call(yAxis);

	let xAxis = d3.axisTop().scale(x);
	this.averageBytesChart
	  .append("g")
	  .attr("class", "x axis")
	  .attr("transform", "translate(0, " + 399 + ")")
	  .call(xAxis);
  };

  _initMessageTypeCountsChart = () => {
	let width = 600, height = 600;
	this.messageTypeCountsChart = d3.select("#message-type-count")
	  .append("svg")
	  .attr("width", width)
	  .attr("height", height)
	  .append("g")
	  .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");

	this.pie = d3.pie().sort(null).value(data => data.count);

  };

  _handleMessage = (msg) => {
    if(msg.type === "METRICS") {
		this._handleMetrics(msg);
	}
  };

  _handleMetrics = (msg) => {
    this._displaySent(msg.totalBytesSent, msg.totalMessagesSent);
    this._displayAverageKbChart(msg.averageBytesPerMessage);
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

  _displayAverageKbChart = (averageBytes) => {
	this._addAverageBytesToDataSet(averageBytes);

    let max = d3.max(this.averageBytes.map(d => d.kb)) * 1.25;
    let minDate = d3.min(this.averageBytes.map(d => d.time));
    let maxDate = d3.max(this.averageBytes.map(d => d.time));

    let timeDomainMin = this._getTimeDomainMin();
    let timeDomainMax = this._getTimeDomainMax();

	let x = d3.scaleTime().domain([timeDomainMin, timeDomainMax]).range([0, 600]);
	let y = d3.scaleLinear().domain([0, max]).range([400, 0]);

	let i = 0;

	let line = d3.line()
	  .x(d => x(d.time))
	  .y(d => y(d.kb));

	let path = this.averageBytesChart.selectAll("path.content")
	  .data([this.averageBytes]);

	path.exit().remove();

	path.enter()
	  .append("path")
	  .attr("class", "content")
	  .style("stroke", "#ddaddd")
	  .style("stroke-width", "2px")
	  .style("fill", "transparent");

	path.attr("d", line(this.averageBytes));

	let yAxis = d3.axisLeft().scale(y);
	this.averageBytesChart.selectAll("g.y.axis")
	  .call(yAxis);

	let xAxis = d3.axisTop().scale(x).ticks(4);
	this.averageBytesChart.selectAll("g.x.axis")
	  .call(xAxis);

  };

  _addAverageBytesToDataSet = (averageBytes) => {
	this.averageBytes.push({kb: (averageBytes / 1000), time: new Date()});
	let timeDomainMin = this._getTimeDomainMin();
	this.averageBytes = this.averageBytes.filter(item => {
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
	this.radius = Math.min(600, 600) / 2;
	let pieColors = d3.scaleOrdinal(["#e57373", "#F06292", "#BA68C8", "#9575CD", "#7986CB", "#64B5F6", "#FFD54F"]);

	let path = d3.arc().outerRadius(this.radius - 150).innerRadius(0);
	let label = d3.arc().outerRadius(200).innerRadius(175);

	let arc = this.messageTypeCountsChart.selectAll(".arc")
	  .data(this.pie(this._transformTypeCountToArray(typeCount)), d => {
	    return Math.random();
	  });

	arc.exit().remove();

	let g = arc.enter()
	  .append("g")
	  .attr("class", "arc");

	g.append("path")
	  .attr("d", path)
	  .attr("fill", d => {
	    return pieColors(d.data.type);
	  });

	g.append("text")
	  .attr("transform", d => {
	    return "translate(" + label.centroid(d) + ")";
	  })
	  .attr("dy", "0.35em")
	  .attr("font-size", "12")
	  .text(d => d.data.type + " (" + d.data.count + ")");

  };

  _transformTypeCountToArray = (typeCount) => {
    let arr = [];
    for(let key in typeCount) {
      if(typeCount.hasOwnProperty(key)) {
		arr.push({type: key, count: typeCount[key]})
	  }
	}
	return arr;
  };

}

new Metrics();