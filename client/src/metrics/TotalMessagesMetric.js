import * as d3 from "d3";

/**
 * Handles the display of total messages sent, but also
 * total bytes/kb/mb sent.
 */
export default class TotalMessagesMetric {

	constructor(socket) {
		socket.addObserver(this._handleTotalMessages);
	}

	_handleTotalMessages = ({totalBytesSent, totalMessagesSent} = msg) => {
		let bytes = d3.select("#total-bytes-sent")
			.selectAll("span")
			.data([totalBytesSent], data => data);

		bytes.exit().remove();

		bytes.enter().append("span").text(data => {
			return "Total bytes sent: "
				+ data + " (" + this._getTruncatedKb(data)
				+ "kB, " + this._getTruncatedMb(data) + "MB). "
				+ "Total messages: " + totalMessagesSent;
		});
	};

	_getTruncatedKb = (bytes) => {
		return ("" + (bytes / 1024)).substr(0, 4);
	};

	_getTruncatedMb = (bytes) => {
		return ("" + ((bytes / 1024) / 1024)).substr(0, 4);
	};

}