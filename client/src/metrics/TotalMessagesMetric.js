import TextD3 from "./TextD3";

/**
 * Handles the display of total messages sent, but also
 * total bytes/kb/mb sent.
 */
export default class TotalMessagesMetric {

	constructor(socket) {
		socket.addObserver(this._handleTotalMessages);
		this._text = new TextD3(document.getElementById("total-bytes-sent"), this._textFunction);
	}

	_handleTotalMessages = ({totalBytesSent, totalMessagesSent} = msg) => {
		this._text.update({bytes: totalBytesSent, totalMessagesSent: totalMessagesSent});
	};

	_textFunction = (data) => {
		return "Total bytes sent: "
			+ data.bytes + " (" + this._getTruncatedKb(data.bytes)
			+ "kB, " + this._getTruncatedMb(data.bytes) + "MB). "
			+ "Total messages: " + data.totalMessagesSent;
	};

	_getTruncatedKb = (bytes) => {
		return ("" + (bytes / 1024)).substr(0, 4);
	};

	_getTruncatedMb = (bytes) => {
		return ("" + ((bytes / 1024) / 1024)).substr(0, 4);
	};

}