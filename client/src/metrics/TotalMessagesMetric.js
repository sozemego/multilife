import TextD3 from "./TextD3";

/**
 * Handles the display of total messages sent/received, but also
 * total bytes/kb/mb sent/received.
 */
export default class TotalMessagesMetric {

	constructor(socket) {
		socket.addObserver(this._handleTotalMessages);
		this._sentText = new TextD3(document.getElementById("total-bytes-sent"), this._sentTextFunction);
		this._receivedText = new TextD3(document.getElementById("total-bytes-received"), this._receivedTextFunction);
	}

	_handleTotalMessages = (msg) => {
		this._sentText.update({
			bytesSent: msg.totalBytesSent,
			messagesSent: msg.totalMessagesSent
		});
		this._receivedText.update({
			bytesReceived: msg.totalBytesReceived,
			messagesReceived: msg.totalMessagesReceived
		});
	};

	_sentTextFunction = (data) => {
		return "Total bytes sent: "
			+ data.bytesSent + " (" + this._getTruncatedKb(data.bytesSent)
			+ "kB, " + this._getTruncatedMb(data.bytesSent) + "MB). "
			+ "Total messages: " + data.messagesSent;
	};

	_receivedTextFunction = (data) => {
		return "Total bytes received: "
			+ data.bytesReceived + " (" + this._getTruncatedKb(data.bytesReceived)
			+ "kB, " + this._getTruncatedMb(data.bytesReceived) + "MB). "
			+ "Total messages: " + data.messagesReceived;
	};

	_getTruncatedKb = (bytes) => {
		return ("" + (bytes / 1024)).substr(0, 4);
	};

	_getTruncatedMb = (bytes) => {
		return ("" + ((bytes / 1024) / 1024)).substr(0, 4);
	};

}