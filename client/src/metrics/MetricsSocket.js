export default class MetricsSocket {

	constructor() {
		this._init();
		this._observers = [];
	}

	_init() {

		this.webSocket = new WebSocket(METRICS_WEBSOCKET_HOST);

		this.webSocket.onopen = () => {

		};

		this.webSocket.onmessage = (msg) => {
			let parsedMsg = JSON.parse(msg.data);
			if(parsedMsg.type === "METRICS") {
				this._observers.forEach(fn => fn(parsedMsg));
			}
		};
	}

	addObserver = (fn) => {
		if(typeof fn !== "function") {
			throw new Error("Pass functions here!");
		}
		this._observers.push(fn);
	};


}