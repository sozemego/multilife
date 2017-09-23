import {notify, on} from "./event-bus";
import {
	CELL_LIST, LOGGED_IN, LOGIN, MAP_DATA,
	PLAYER_IDENTITY, PLAYER_POINTS,
	SHAPE_PLACED, PLAYER_ADDED,
	TICK_DATA, PLAYER_REMOVED,
	TIME_REMAINING
} from "./events";
import {throwError} from "./utils";

const messageTypeMarkers = {
	[CELL_LIST]: 1,
	[MAP_DATA]: 2,
	[PLAYER_IDENTITY]: 3,
	[TICK_DATA]: 5,
	[TIME_REMAINING]: 7,
	[PLAYER_ADDED]: 9,
	[PLAYER_REMOVED]: 10,
	[PLAYER_POINTS]: 11
};

let webSocket = null;
let connected = false;

const onCellList = msg => {
	const cellList = handleByteCellList(msg);
	notify(CELL_LIST, cellList);
};

const onMapData = msg => {
	const data = handleByteMapData(msg);
	notify(MAP_DATA, data);
};

const onPlayerIdentity = msg => {
	const playerId = handleBytePlayerIdentity(msg);
	notify(LOGGED_IN);
	notify(PLAYER_IDENTITY, playerId);
};

const onTickData = msg => {
	const data = handleByteTickData(msg);
	notify(TICK_DATA, data);
};

const onTimeRemaining = msg => {
	const data = handleByteTimeRemaining(msg);
	notify(TIME_REMAINING, data);
};

const onPlayerAdded = msg => {
	const newPlayerData = handleBytePlayerAdded(msg);
	notify(PLAYER_ADDED, newPlayerData);
};

const onPlayerRemoved = msg => {
	const playerId = handleBytePlayerRemoved(msg);
	notify(PLAYER_REMOVED, playerId);
};

const onPlayerPoints = msg => {
	const data = handleBytePlayerPoints(msg);
	notify(PLAYER_POINTS, data);
};

const handleByteMessage = msg => {
	const messageTypeMarker = msg[0];
	switch (messageTypeMarker) {
		case messageTypeMarkers[CELL_LIST]:
			onCellList(msg);
			break;
		case messageTypeMarkers[MAP_DATA]:
			onMapData(msg);
			break;
		case messageTypeMarkers[PLAYER_IDENTITY]:
			onPlayerIdentity(msg);
			break;
		case messageTypeMarkers[TICK_DATA]:
			onTickData(msg);
			break;
		case messageTypeMarkers[TIME_REMAINING]:
			onTimeRemaining(msg);
			break;
		case messageTypeMarkers[PLAYER_ADDED]:
			onPlayerAdded(msg);
			break;
		case messageTypeMarkers[PLAYER_REMOVED]:
			onPlayerRemoved(msg);
			break;
		case messageTypeMarkers[PLAYER_POINTS]:
			onPlayerPoints(msg);
			break;
		default:
			console.warn("Unrecognized message marker: ", messageTypeMarker);
	}
};

const convertBytesToInt32 = bytes => {
	return new DataView(bytes.buffer).getInt32(0, false);
};

const convertBytesToInt16 = bytes => {
	return new DataView(bytes.buffer).getInt16(0, false);
};

const convertBytesToFloat = bytes => {
	return new DataView(bytes.buffer).getFloat32(0, false);
};

const convertBytesToString = bytes => {
	let result = "";
	for (let i = 0; i < bytes.length; i += 2) {
		const charBytes = bytes.slice(i, i + 2);
		result += String.fromCharCode(convertBytesToInt16(charBytes));
	}
	return result;
};

const convertByteToBoolean = byte => {
	return byte === 1;
};

const handleByteCellList = data => {
	const bytesPerCell = 13;
	const cellCount = (data.length - 1) / bytesPerCell;
	const cells = [];
	let offset = 1;
	for (let i = 0; i < cellCount; i++) {
		const x = convertBytesToInt32(data.slice(offset, offset + 4));
		const y = convertBytesToInt32(data.slice(offset + 4, offset + 8));
		const alive = convertByteToBoolean(data[offset + 8]);
		const ownerId = convertBytesToInt32(data.slice(offset + 9, offset + 13));
		cells.push({
			x, y, alive, ownerId
		});
		offset += bytesPerCell;
	}

	return cells;
};

const handleBytePlayerIdentity = msg => {
	return convertBytesToInt32(msg.slice(1));
};

const handleByteMapData = msg => {
	return {
		width: convertBytesToInt32(msg.slice(1, 5)),
		height: convertBytesToInt32(msg.slice(5))
	}
};

const handleByteTickData = msg => {
	return {
		iterations: convertBytesToInt32(msg.slice(1))
	}
};

const handleByteTimeRemaining = msg => {
	return {
		remainingTime: convertBytesToFloat(msg.slice(1))
	}
};

const handleBytePlayerAdded = msg => {
	const playerData = {};
	playerData.playerId = convertBytesToInt32(msg.slice(1, 5));
	playerData.color = convertBytesToInt32(msg.slice(5, 9));
	playerData.name = convertBytesToString(msg.slice(9));
	return playerData;
};

const handleBytePlayerRemoved = (msg) => {
	return convertBytesToInt32(msg.slice(1, 5));
};

const handleBytePlayerPoints = msg => {
	const playerId = convertBytesToInt32(msg.slice(1, 5));
	const points = convertBytesToInt32(msg.slice(5));
	return {
		playerId,
		points
	};
};

const handleMessage = msg => {
	notify(msg.type, msg);
};

const getBytesClickMessage = indices => {
	const buffer = new ArrayBuffer((indices.length * 4) + 4);
	const bufferView = new Uint32Array(buffer);
	bufferView[0] = 1;
	for (let i = 0; i < indices.length; i++) {
		bufferView[i + 1] = indices[i];
	}
	return buffer;
};

const openConnection = webSocketPath => {

	webSocket = new WebSocket(webSocketPath);
	webSocket.binaryType = "arraybuffer";

	webSocket.onopen = () => {
		connected = true;
	};

	webSocket.onmessage = msg => {
		if (msg.data instanceof ArrayBuffer) {
			handleByteMessage(new Uint8Array(msg.data));
		} else {
			handleMessage(JSON.parse(msg.data));
		}
	};
};

const onLogin = name => {
	if (typeof name !== "string") {
		throwError("Name needs to be a string, it was: " + name);
	}

	webSocket.send(JSON.stringify({name, type: "LOGIN"}));
};

const onShapePlaced = indices => {
	if (!connected) {
		return;
	}
	webSocket.send(getBytesClickMessage(indices));
};

// this._pingMessage = new Uint8Array([3]).buffer;

export const createNetworkLayer = webSocketPath => {
	if (typeof webSocketPath !== "string") {
		throwError("webSocketPath needs to be a string, it was: " + webSocketPath);
	}

	on(LOGIN, onLogin);
	on(SHAPE_PLACED, onShapePlaced);

	openConnection(webSocketPath);
};