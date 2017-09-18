export const metricsSocket = () => {

	const observers = [];
	let webSocket = undefined;

	const metricSocket = {};

	metricSocket.init = path => {

		webSocket = new WebSocket(path);
		webSocket.onopen = () => {};

		webSocket.onmessage = msg => {
			const parsedMsg = JSON.parse(msg.data);
			if(parsedMsg.type === "METRICS") {
				observers.forEach(fn => fn(parsedMsg));
			}
		};
	};

	metricSocket.addObserver = observer => {
		if(typeof observer !== "function") {
			throw new Error("Observer has to be a function.");
		}
		observers.push(observer);
	};

	return metricSocket;
};