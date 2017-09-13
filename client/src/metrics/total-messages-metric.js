import TextD3 from "./TextD3";

const sentTextFunction = (data) => {
	return "Total bytes sent: "
		+ data.bytesSent + " (" + getTruncatedKb(data.bytesSent)
		+ "kB, " + getTruncatedMb(data.bytesSent) + "MB). "
		+ "Total messages: " + data.messagesSent;
};

const receivedTextFunction = (data) => {
	return "Total bytes received: "
		+ data.bytesReceived + " (" + getTruncatedKb(data.bytesReceived)
		+ "kB, " + getTruncatedMb(data.bytesReceived) + "MB). "
		+ "Total messages: " + data.messagesReceived;
};

const getTruncatedKb = (bytes) => {
	return ("" + (bytes / 1024)).substr(0, 4);
};

const getTruncatedMb = (bytes) => {
	return ("" + ((bytes / 1024) / 1024)).substr(0, 4);
};

/**
 * Handles the display of total messages sent/received, but also
 * total bytes/kb/mb sent/received.
 */
export const totalMessagesMetric = (socket) => {

	const sentText = new TextD3(document.getElementById("total-bytes-sent"), sentTextFunction);
	const receivedText = new TextD3(document.getElementById("total-bytes-received"), receivedTextFunction);

	const handleTotalMessages = (msg) => {
		sentText.update({
			bytesSent: msg.totalBytesSent,
			messagesSent: msg.totalMessagesSent
		});
		receivedText.update({
			bytesReceived: msg.totalBytesReceived,
			messagesReceived: msg.totalMessagesReceived
		});
	};

	socket.addObserver(handleTotalMessages);

};
