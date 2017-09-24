import {assertIsFunction, assertIsString} from '../assert';

export const metricsSocket = () => {

	const observers = [];
	let webSocket = undefined;

	const metricSocket = {};

	metricSocket.init = path => {
		assertIsString(path);

		webSocket = new WebSocket(path);
		webSocket.onopen = () => { };

		webSocket.onmessage = msg => {
			const parsedMsg = JSON.parse(msg.data);
			if (parsedMsg.type === 'METRICS') {
				observers.forEach(fn => fn(parsedMsg));
			}
		};
	};

	metricSocket.addObserver = observer => {
		assertIsFunction(observer);
		observers.push(observer);
	};

	return metricSocket;
};