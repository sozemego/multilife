import p5 from "p5";
import Simulation from "./Simulation";
import Cell from "./Cell";

class Game {

	keys = {
		Q: 81,
		W: 87,
		E: 69,
		R: 82,
		A: 65,
		S: 83,
		D: 68
	};

	constructor(canvas, sketch) {
		this.sketch = sketch;
		this.ticks = 0;
		this.FPS = 60;
		this.stepsPerSecond = 4;
		this.stepPerFrames = this.FPS / this.stepsPerSecond;
		this.simulationSteps = -1;
		this.canvas = canvas;
		this.canvas.mouseReleased(this._onMouseUp);
		this._styleCanvas(canvas);
		this.cellSize = 10;
		this.simulation = new Simulation(0, 0, {}, this.cellSize, this.rectRenderFunction);
		this.cells = [];
		this.width = 0;
		this.height = 0;
		this.websocketHost = WEBSOCKET_HOST;
		this.webSocket = undefined;
		this.myId = 0;
		this.connected = false;
		this.recentlyClicked = false;
		this.playerData = {};
		this.actualCells = [];
		this.firstMapData = true;
		this.shapeMap = {};
		this.offsets = {};
		this.onWindowResize(window.innerWidth, window.innerHeight);
		this._initShapes();
		this._createLoginView();
		this._pingMessage = new Uint8Array([3]).buffer;
	}

	/**
	 * Creates shapes that player is able to spawn.
	 * @private
	 */
	_initShapes = () => {
		let shapeMap = this.shapeMap;
		let keys = this.keys;
		// INIT BASIC RULES
		shapeMap[keys.Q] = {name: "block", shape: this._parseShape("11,11")};
		shapeMap[keys.W] = {name: "hive", shape: this._parseShape("0110,1001,0110")};
		shapeMap[keys.E] = {name: "loaf", shape: this._parseShape("0010,0101,1001,0110")};
		shapeMap[keys.R] = {name: "tub", shape: this._parseShape("010,101,010")};
		shapeMap[keys.A] = {name: "blinker", shape: this._parseShape("1,1,1")};
		shapeMap[keys.S] = {
			name: "floodgate",
			shape: this._parseShape("1110000,0100000,0000000,0000000,0000001,0000011,0000001")
		};
		this._renderAvailableShapes();
	};

	/**
	 * Parses a given string into a shape. Each row in a shape is delimited with a comma.
	 * Character 0 means dead cell, 1 means alive cell.
	 * Returns an array of objects, where each object specifies the offset
	 * (x, y) form origin (top-left) and specifies if a cell is dead or alive.
	 * @param str
	 * @returns {Array}
	 * @private
	 */
	_parseShape = (str) => {
		let tokens = str.split(",");
		let rows = tokens.length;
		let columns = tokens[0].length;
		let offsets = [];
		for (let i = 0; i < rows; i++) {
			for (let j = 0; j < columns; j++) {
				let bit = tokens[i].charAt(j);
				offsets.push({x: i, y: j, bit: bit});
			}
		}
		return offsets;
	};

	_createLoginView = () => {
		let dom = document.getElementById("login");

		let name = document.createElement("input");
		name.setAttribute("id", "name");
		name.setAttribute("placeholder", "Type your name");
		dom.appendChild(name);
	  	name.focus();

		let button = document.createElement("button");
		button.appendChild(document.createTextNode("ENTER!"));
		button.classList.add("login-button");
		button.addEventListener("click", (event) => {
			this._login();
		});

		dom.appendChild(button);

	};

	_login = () => {
		let name = document.getElementById("name").value.trim();
		if (!name) {
			return;
		}

		this.webSocket = new WebSocket(this.websocketHost);
		this.webSocket.onopen = () => {
			this.webSocket.send(JSON.stringify({name, type: "LOGIN"}));
			document.getElementById("login-container").classList.add("logged");
			this.connected = true;
			this._renderAvailableShapes();
		};

		this.webSocket.onmessage = (msg) => {
			if(msg.data instanceof ArrayBuffer) {
				this._handleByteMessage(new Uint8Array(msg.data));
			} else {
				this._handleMessage(JSON.parse(msg.data));
			}
		};

		this.webSocket.binaryType = "arraybuffer";
	};

	_styleCanvas = (p5Canvas) => {
		p5Canvas.canvas.classList.add("canvas");
	};

	/**
	 * Returns index of a cell that is (xOffset, yOffset) away from mouse cursor.
	 * xOffset and yOffset should be in pixels.
	 * @param xOffset
	 * @param yOffset
	 * @private
	 */
	_getIndexOffsetFromMouse = (xOffset, yOffset) => {
		return this._getIndex(
			this.sketch.mouseX + (xOffset * this.cellSize),
			this.sketch.mouseY + (yOffset * this.cellSize)
		);
	};

	/**
	 * Returns a index in the cell array of a point at location x, y.
	 * x, y represents a point on a canvas (in pixels).
	 * @param x
	 * @param y
	 * @returns {number}
	 * @private
	 */
	_getIndex = (x, y) => {
		x = x / this.cellSize;
		y = y / this.cellSize;
		let index = Math.floor(x) + (Math.floor(y) * this.width);
		let maxSize = this.width * this.height;
		if (index < 0) return index + (maxSize); // wrap around if neccesary
		if (index >= maxSize) return index % (maxSize);
		return index; // return index if not neccesary to wrap
	};

	draw = () => {
		this.sketch.background(245);
		this.ticks++;
		this.onWindowResize(window.innerWidth, window.innerHeight);
		this._update();
		this._render();
	};

	selectShape(keyCode) {
		this.selectedShape = this.shapeMap[keyCode];
		this._renderAvailableShapes();
	}

	_renderAvailableShapes = () => {

		if (!this.connected) {
			return;
		}

		let keys = this.keys;
		let shapeMap = this.shapeMap;

		let dom = document.getElementById("available-shapes");
		dom.innerHTML = "";

		for (let k in shapeMap) {
			if (shapeMap.hasOwnProperty(k)) {

				let shape = shapeMap[k];
				let shapeName = shape.name;
				let key = this._getKey(keys, k);

				let container = document.createElement("div");
				container.addEventListener("click", () => {
					this.selectShape(k);
				});

				let textElement = document.createElement("span");
				textElement.style.display = "inline-block";
				textElement.appendChild(document.createTextNode(key + " " + shapeName));

				if (this.selectedShape && shapeName === this.selectedShape.name) {
					textElement.style.backgroundColor = "red";
				}

				container.appendChild(textElement);
				dom.appendChild(container);
			}
		}

	};

	_getKey = (obj, value) => {
		for (let key in obj) {
			if (obj.hasOwnProperty(key)) {
				if (obj[key] == value) {
					return key;
				}
			}
		}
	};

	_renderPlayerPoints = () => {
		if (!this.playerData) {
			return;
		}

		let dom = document.getElementById("player-points");
		dom.innerHTML = "";

		for(let playerId in this.playerData) {
			if (this.playerData.hasOwnProperty(playerId)) {
				let color = this.playerData[playerId].color;
				let name = this.playerData[playerId].name;
				let points = this.playerData[playerId].points;

				let listElement = document.createElement("div");
				listElement.classList.add("player-points-element");

				let playerColorElement = document.createElement("span");
				playerColorElement.classList.add("player-points-color");
				playerColorElement.style.backgroundColor = this._convertIntToHexColor(color);

				listElement.appendChild(playerColorElement);

				points = points === undefined ? "0" : points;

				let nameElement = document.createElement("span");
				nameElement.classList.add("player-points-name");
				let nameNode = document.createTextNode(name);
				nameElement.appendChild(nameNode);
				listElement.appendChild(nameElement);

				let pointsElement = document.createElement("span");
				pointsElement.classList.add("player-points-points");

				let pointsNode = document.createTextNode(points);
				pointsElement.appendChild(pointsNode);

				listElement.appendChild(pointsElement);

				dom.appendChild(listElement);
			}
		}

	};

	/**
	 Updates the simulation.
	 */
	_update = () => {
		if (!this.connected) {
			return;
		}
		if (this.ticks % this.stepPerFrames === 0) {
			//advanceSimulation();
			this.recentlyClicked = false;
		}
	};

	_advanceSimulation = () => {
		this.simulation.update();
		this.simulationSteps++;
		this._sendPingMessage();
	};

	/**
	 Renders cells.
	 */
	_render = () => {
		this._renderPlayerPoints();
		this._renderSelectedShape();
		this.simulation.render(this._getViewport());
	};

	_renderSelectedShape = () => {
		if (!this.selectedShape) {
			return;
		}

		this._translateMouse();

		let shape = this.selectedShape.shape;
		let indices = this._findIndices(shape.filter((p) => p.bit === "1").map((p) => {
			return {x: p.x, y: p.y}
		}));

		let positions = this._findPositions(indices);
		for (let i = 0; i < positions.length; i++) {
			let p = positions[i];
			this.rectRenderFunction(
				p.x + (1 - this.cellSize) * 0.5, p.y + (1 - this.cellSize) * 0.5,
				this.cellSize, this.cellSize, "#adeedd");
		}

		this._untranslateMouse();

	};

	_translateMouse = () => {
		this.mouseSnapshotX = this.sketch.mouseX;
		this.mouseSnapshotY = this.sketch.mouseY;

		this.sketch.mouseX -= this.offsets.x;
		this.sketch.mouseY -= this.offsets.y;
	};

	_untranslateMouse = () => {
		if (this.mouseSnapshotX === undefined || this.mouseSnapshotY === undefined) {
			throw new Error("You did not translate");
		}
		this.sketch.mouseX = this.mouseSnapshotX;
		this.sketch.mouseY = this.mouseSnapshotY;
	};

	/**
	 * Finds x,y positions of a given array of indices.
	 * The indices are indices of a 1D array which represents
	 * cells.
	 * Returned positions are multiplied by cellSize, so they represent
	 * coordinates were cell should be drawn.
	 * @param indices
	 * @returns {Number|*|Array}
	 * @private
	 */
	_findPositions = (indices) => {
		return indices.map((i) => {
			let x = i % this.width;
			let y = Math.floor(i / this.width);
			return {x: x * this.cellSize, y: y * this.cellSize};
		});
	};

	/**
	 * Finds indices in 1D array of x, y positions.
	 * The positions are translated by current mouse coorinates,
	 * so they are positions in relation to the mouse cursor.
	 * @param positions
	 * @returns {Array}
	 * @private
	 */
	_findIndices = (positions) => {
		let indices = [];
		for (let i = 0; i < positions.length; i++) {
			indices.push(this._getIndexOffsetFromMouse(positions[i].x, positions[i].y));
		}
		return indices;
	};

	_getViewport = () => {
		return {
			x: window.scrollX,
			y: window.scrollY,
			width: window.innerWidth,
			height: window.innerHeight
		};
	};

	_handleMessage = (msg) => {
		if (msg.type === "PLAYER_IDENTITY") {
			this.myId = msg.playerId;
		}
		if (msg.type === "PLAYER_DATA") {
			this._onPlayerData(msg);
		}
		if (msg.type === "MAP_DATA") {
			this._onMapData(msg);
		}
		if (msg.type === "CELL_LIST") {
			this._onMapUpdate(msg);
		}
		if (msg.type === "TICK_DATA") {
			this._onTickData(msg);
		}
		if (msg.type === "TIME_REMAINING") {
			this._onRemainingTime(msg);
		}
	};

	_handleByteMessage(msg) {
		if(msg[0] === 1) {
			const data = this._handleByteCellList(msg);
			this._onMapUpdate(data);
		}
		if(msg[0] === 2) {
			const data = this._handleByteMapData(msg);
			this._onMapData(data);
		}
		if(msg[0] === 3) {
			this.myId = this._handleBytePlayerIdentity(msg);
		}
		if(msg[0] === 5) {
			const data = this._handleByteTickData(msg);
			this._onTickData(data);
		}
		if(msg[0] === 7) {
			const data = this._handleByteTimeRemaining(msg);
			this._onRemainingTime(data);
		}
		if(msg[0] === 9) {
			this._handleBytePlayerAdded(msg);
		}
		if(msg[0] === 10) {
			this._handleBytePlayerRemoved(msg);
		}
		if(msg[0] === 11) {
			this._handleBytePlayerPoints(msg);
		}
	}

	_onPlayerData = (msg) => {
		this.playerData = msg;
		this.simulation.setPlayerData(msg);
		this._renderPlayerPoints();
	};

	_handleByteMapData = (msg) => {
		return {
			width: this._convertBytesToInt32(msg.slice(1, 5)),
			height: this._convertBytesToInt32(msg.slice(5))
		}
	};

	_onMapData = (data) => {
		this.width = data.width;
		this.height = data.height;
		this.simulation = new Simulation(this.width, this.height, this.playerData, this.cellSize, this.rectRenderFunction);
	};

	_handleByteCellList = (data) => {

		const bytesPerCell = 13;
		const cellCount = (data.length - 1) / bytesPerCell;
		const cells = [];
		let offset = 1;
		for(let i = 0; i < cellCount; i++) {
			const x = this._convertBytesToInt32(data.slice(offset, offset + 4));
			const y = this._convertBytesToInt32(data.slice(offset + 4, offset + 8));
			const alive = this._convertByteToBoolean(data[offset + 8]);
			const ownerId = this._convertBytesToInt32(data.slice(offset + 9, offset + 13));
			cells.push({
				x, y, alive, ownerId
			});
			offset += bytesPerCell;
		}

		return {cells};
	};

	_convertBytesToInt32 = (bytes) => { //TODO make static util functions
		return new DataView(bytes.buffer).getInt32(0, false);
	};

	_convertBytesToInt16 = (bytes) => { //TODO make static util functions
		return new DataView(bytes.buffer).getInt16(0, false);
	};

	_convertBytesToFloat = (bytes) => { //TODO make static util functions
		return new DataView(bytes.buffer).getFloat32(0, false);
	};

	_convertByteToBoolean = (byte) => { //TODO make static util functions
		return byte === 1;
	};

	_convertIntToHexColor = (int) => {
		int >>>= 0;
		const b = int & 0xFF;
		const g = (int & 0xFF00) >>> 8;
		const r = (int & 0xFF0000) >>> 16;
		return "rgb(" + [r, g, b].join(",") + ")";
	};

	_convertBytesToString(bytes) { //TODO make static util functions
		let result = "";
		for(let i = 0; i < bytes.length; i += 2) {
			const charBytes = bytes.slice(i, i + 2);
			result += String.fromCharCode(this._convertBytesToInt16(charBytes));
		}
		return result;
	}

	_onMapUpdate = (data) => {
		let newCells = data.cells;
		if (newCells.length === this.width * this.height) {
			this.actualCells = [];
			console.log("Received all data.");
			for (let i = 0; i < newCells.length; i++) {
				let newCell = newCells[i];
				let {x, y, alive, ownerId} = newCell;
				let cell = new Cell(x, y, alive, ownerId, this.cellSize, "#0000ff", this.rectRenderFunction);
				if (alive) {
					cell.currentPercentageSize = 1;
				}
				this.actualCells.push(cell);
			}
			if (!this.firstMapData) {
				return;
			}
		}
		for (let i = 0; i < newCells.length; i++) {
			let newCell = newCells[i];
			this.simulation.setCellState({x: newCell.x, y: newCell.y}, newCell.alive, newCell.ownerId);
		}
		if (this.firstMapData) {
			this._advanceSimulation();
			this.firstMapData = false;
		}
	};

	_handleByteTickData = (msg) => {
		return {
			iterations: this._convertBytesToInt32(msg.slice(1))
		}
	};

	_onTickData = (data) => {
		if (this.simulationSteps === -1) {
			this.simulationSteps = data.iterations;
		}
		let tick = data.iterations;
		let difference = tick - this.simulationSteps;
		console.log("Sent tick " + tick + ". Client side ticks " + this.simulationSteps);
		this._advanceSimulation();
		if (difference > 0) {
			//advanceSimulation();
			console.log("Tick difference of " + difference + ", advancing.");
		}
	};

	_handleBytePlayerIdentity = (msg) => {
		return this._convertBytesToInt32(msg.slice(1));
	};

	getFPS = () => {
		return this.FPS;
	};

	rectRenderFunction = (x, y, width, height, color) => {
		let pos = this._translateOffsets(x, y);
		this.sketch.fill(color);
		this.sketch.rect(pos.x, pos.y, width, height);
	};

	ellipseRenderFunction = (x, y, width, height, color) => {
		let pos = this._translateOffsets(x, y);
		this.sketch.fill(color);
		this.sketch.ellipse(pos.x + width / 2, pos.y + height / 2, width, height);
	};

	_onMouseUp = () => {
		if (this.recentlyClicked || !this.connected) {
			return;
		}

		if (this.selectedShape) {
			this._sendShape(this.selectedShape.shape);
			this.recentlyClicked = true;
		}
	};

	/**
	 * Tries to inform the back-end that a players wants to spawn a given shape.
	 * @param shape
	 * @private
	 */
	_sendShape = (shape) => {
		if (!shape || !this.webSocket) {
			return;
		}

		this._translateMouse();

		let indices = [];
		for (let i = 0; i < shape.length; i++) {
			let el = shape[i];
			if (el.bit === "1") {
				let index = this._getIndexOffsetFromMouse(el.x, el.y);
				indices.push(index);
			}
		}
		this._untranslateMouse();
		// this.webSocket.send(JSON.stringify({type: "CLICK", indices: indices}));
		this.webSocket.send(this._getBytesClickMessage(indices));
	};

	_getBytesClickMessage = (indices) => {
		const buffer = new ArrayBuffer((indices.length * 4) + 4);
		const bufferView = new Uint32Array(buffer);
		bufferView[0] = 1;
		for(let i = 0; i < indices.length; i++) {
			bufferView[i + 1] = indices[i];
		}
		return buffer;
	};

	/**
	 * Called when a window is resized. Used to calculate stuff based on window size
	 * (e.g. offset from top-left so that rendering area is in the middle).
	 * @param width
	 * @param height
	 */
	onWindowResize(width, height) {
		let widthTaken = this.width * this.cellSize;
		let heightTaken = this.height * this.cellSize;
		this.offsets.x = (width - widthTaken) / 2;
		this.offsets.y = (height - heightTaken) / 2;
	}

	/**
	 * Translates x and y coordinates of a cell, so that the rendering area
	 * is in the middle of the screen.
	 * @param x
	 * @param y
	 * @private
	 */
	_translateOffsets = (x, y) => {
		return {
			x: x + this.offsets.x,
			y: y + this.offsets.y
		};
	};

	_handleByteTimeRemaining = (msg) => {
		return {
			remainingTime: this._convertBytesToFloat(msg.slice(1))
		}
	};

	/**
	 * Handler for when the client receives RemainingTime message.
	 * @param msg
	 * @private
	 */
	_onRemainingTime = (msg) => {
		let remainingTime = msg.remainingTime;

		let dom = document.getElementById("remaining-time");
		dom.innerHTML = "";

		let span = document.createElement("span");
		span.appendChild(document.createTextNode(this._parseRemainingTime(remainingTime)));
		dom.appendChild(span);

	};

	/**
	 * Parses remaining time in milliseconds to be in the mm:ss format.
	 * @param remainingTime
	 * @returns {string}
	 * @private
	 */
	_parseRemainingTime = (remainingTime) => {
		remainingTime = parseInt(remainingTime);
		let second = Math.max(Math.floor((remainingTime / 1000) % 60), 0);
		let minute = Math.max(Math.floor((remainingTime / (1000 * 60)) % 60), 0);

		second = "" + second;
		second = ("00" + second).substr(second.length);

		minute = "" + minute;
		minute = ("00" + minute).substr(minute.length);

		return minute + ":" + second;
	};

	_handleBytePlayerAdded = (msg) => {
		const {playerData} = this;
		const playerId = this._convertBytesToInt32(msg.slice(1, 5));
		const playerColorInt = this._convertBytesToInt32(msg.slice(5, 9));
		const playerName = this._convertBytesToString(msg.slice(9));
		const obj = {};
		obj.color = playerColorInt;
		obj.name = playerName;
		playerData[playerId] = obj;
	};

	_handleBytePlayerRemoved = (msg) => {
		const {playerData} = this;
		const playerId = this._convertBytesToInt32(msg.slice(1, 5));
		delete playerData[playerId];
	};

	_handleBytePlayerPoints = (msg) => {
		const {playerData} = this;
		const playerId = this._convertBytesToInt32(msg.slice(1, 5));
		playerData[playerId].points = this._convertBytesToInt32(msg.slice(5));
	};

	_sendPingMessage = () => {
		this.webSocket.send(this._pingMessage);
	};

}

let sketch = new p5((p) => {

	let game;

	p.setup = () => {
		game = new Game(p.createCanvas(window.innerWidth, window.innerHeight), p);
		p.frameRate(game.getFPS());
	};

	p.keyPressed = (event) => {
		if (game.connected) {
			game.selectShape(event.keyCode);
		}
	};

	p.draw = () => {
		game.draw();
	};

	p.mouseReleased = () => {
		game._onMouseUp();
	};

	p.windowResized = () => {
		p.resizeCanvas(
			Math.max(window.innerWidth, game.width * game.cellSize),
			Math.max(window.innerHeight, game.height * game.cellSize)
		);
		game.onWindowResize(window.innerWidth, window.innerHeight);
	};

});

