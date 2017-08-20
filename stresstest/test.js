let W3CWebSocket = require('websocket').w3cwebsocket;
let args = require("minimist")(process.argv.slice(2));
console.log(args);

let maxPlayers = parseInt(args["players"]) || 50;
let loginDelay = parseInt(args["loginDelay"]) || 15;
let path = args["path"] || "ws://127.0.0.1:8000/game";
let click = args["click"] == "true";
console.log(click);

let loginNew = function(players) {
	console.log("Logging new, remaining: " + players);
	if(!players) {
		// process.exit();
		return;
	}
	let ws = new W3CWebSocket(path);
	let width = 0, height = 0;
	let connected = false;

	let name = "player";

	ws.onopen = function() {
		connected = true;
		console.log("Connected");
		ws.send(JSON.stringify(getLoginObject(name)));
	};

	ws.onmessage = function(msg) {
		// handleMessage(JSON.parse(msg.data));
	};

	function getLoginObject(name) {
	    return {type: "LOGIN", name: name};
	}

	function handleMessage(msg) {
	    if(msg.type === "PLAYER_IDENTITY") {
	        //myId = msg.playerId;
	    }
	    if(msg.type === "PLAYER_DATA") {
	        //onPlayerData(msg);
	    }
	    if(msg.type === "MAP_DATA") {
	        width = msg.width;
	        height = msg.height;
	    }
	    if(msg.type === "CELL_LIST") {
	        //onMapUpdate(msg);
	    }
	    if(msg.type === "TICK_DATA") {
	    	//onTickData(msg);
	    }
	    if(msg.type === "PONG") {
	    	pings.push(Date.now() - t0);
	    	pringCurrentAndAveragePing();
	    }
	    //console.log("got msg", msg);
	}

	function sendClick() {
		if(!connected) {
			return;
		}

		let number = getRandomInt(5, 55);

		let indices = [];
		for(let i = 0; i < number; i++) {
			let randomIndex = getRandomInt(0, width * height);
			indices.push(randomIndex);
		}

		// const message = JSON.stringify({type:"CLICK", indices: indices});
		const message = _getBytesClickMessage(indices);

		try {
			ws.send(message);
		} catch (e) {
			console.log(e);
			connected = false;
		}
	}

	function _getBytesClickMessage(indices) {
		const buffer = new ArrayBuffer((indices.length * 4) + 4);
		const bufferView = new Uint32Array(buffer);
		bufferView[0] = 1;
		for(let i = 0; i < indices.length; i++) {
			bufferView[i + 1] = indices[i];
		}
		return buffer;
	}

	let pings = [];
	let t0 = 0;
	let pingMessage = JSON.stringify({type: "PING"});

	function ping() {
		if(!connected) {
			return;
		}
		t0 = Date.now();
		ws.send(pingMessage);
	}

	function pringCurrentAndAveragePing() {
		if(pings.length === 0) {
			return;
		}
		let lastPing = pings[pings.length - 1];
		console.log("Last ping in milliseconds: ", lastPing);

		let sum = 0;
		for(let i = 0; i < pings.length; i++) {
			sum += pings[i];
		}
		console.log("Average ping: ", sum / pings.length);
	}

	if(players === maxPlayers) {
		setInterval(ping, 1000);
	}
	if(click) {
		setInterval(sendClick, getRandomInt(25, 1250));
	}
	setTimeout(loginNew, loginDelay, --players);
};

setTimeout(loginNew, loginDelay, maxPlayers);

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
