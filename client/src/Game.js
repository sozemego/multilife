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
	this.FPS = 30;
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
  	this.webSocket = undefined;
  	this.myId = 0;
  	this.playerColors = {};
  	this.rules = ["BASIC"];
	this.connected = false;
	this.recentlyClicked = false;
	this.playerData = {};
	this.actualCells = [];
	this.firstMapData = true;
	this.shapeMap = {};
	this._initShapes();
	this._createLoginView();
  }

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
	if(!shape || !this.webSocket) {
	  return;
	}

	let indices = [];
	for(let i = 0; i < shape.length; i++) {
	  let el = shape[i];
	  if(el.bit === "1") {
		let index = this._getIndexMouseWithOffset(el.x, el.y);
		indices.push(index);
	  }
	}
	this.webSocket.send(JSON.stringify({type: "CLICK", indices: indices}));
  };

  /**
   * Creates shapes that player is able to spawn.
   * @private
   */
  _initShapes = () => {
    let shapeMap = this.shapeMap;
    let keys = this.keys;
	// INIT BASIC RULES
	shapeMap[keys.Q] = { name: "block", shape: this._parseShape("11,11") };
	shapeMap[keys.W] = { name: "hive", shape: this._parseShape("0110,1001,0110") };
	shapeMap[keys.E] = { name: "loaf", shape: this._parseShape("0010,0101,1001,0110") };
	shapeMap[keys.R] = { name: "tub", shape: this._parseShape("010,101,010") };
	shapeMap[keys.A] = { name: "blinker", shape: this._parseShape("1,1,1") };
	shapeMap[keys.S] = { name: "floodgate", shape: this._parseShape("1110000,0100000,0000000,0000000,0000001,0000011,0000001") };

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

	let button = document.createElement("button");
	button.appendChild(document.createTextNode("ENTER!"));
	button.classList.add("login-button");
	button.addEventListener("click", (event) => {
	  this._login();
	});

	dom.appendChild(button);

  };

  _login = () => {
	let name = document.getElementById("name").value;
	if (!name) {
	  return;
	}

	this.webSocket = new WebSocket("ws://127.0.0.1:8080/game");
	this.webSocket.onopen = () => {
	  this.webSocket.send(JSON.stringify({type: "LOGIN", name: name, rule: "BASIC"}));
	  document.getElementById("login-container").classList.add("logged");
	};

	this.webSocket.onmessage = (msg) => {
	  this._handleMessage(JSON.parse(msg.data));
	};
  };

  _styleCanvas = (p5Canvas) => {
	p5Canvas.canvas.classList.add("canvas");
  };

  _getIndexMouseWithOffset = (xOffset, yOffset) => {
	return this._getIndex(this.sketch.mouseX + (xOffset * this.cellSize), this.sketch.mouseY + (yOffset * this.cellSize));
  };

  /**
   Returns a index in the cell array of a point at location x, y.
   x, y represents a point on a canvas (in pixels).
   */
  _getIndex = (x, y) => {
	if (x > this.cellSize * this.width || y > this.cellSize * this.height) {
	  return;
	}
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
	this._renderPlayerPoints();
	this._renderAvailableShapes();
	this._update();
	this._render();
  };

  _renderAvailableShapes = () => {

    let keys = this.keys;
    let shapeMap = this.shapeMap;

	let fontSize = 17;
	let y = this.canvas.height;
	let x = 5 + window.scrollX;
	let i = 0;

    for(let k in shapeMap) {
      if(shapeMap.hasOwnProperty(k)) {

        let shape = shapeMap[k];
        let shapeName = shape.name;
        let key = this._getKey(keys, k);
		this.sketch.textSize(fontSize);
		this.sketch.fill(0);
		this.sketch.text(key + ": " + shapeName, x, y - (i * fontSize));

		i++;
	  }
	}

  };

  _getKey = (obj, value) => {
    for(let key in obj) {
      if(obj.hasOwnProperty(key)) {
		if(obj[key] == value) {
		  return key;
		}
	  }
	}
  };

  _renderPlayerPoints = () => {
	if (!this.playerData) {
	  return;
	}
	let x = 5 + window.scrollX;
	let y = 25 + window.scrollY;
	let size = 17;
	let spacing = 4;

	let i = 0;
	for (let o in this.playerData.colors) {
	  if (this.playerData.colors.hasOwnProperty(o)) {
		let color = this.playerData.colors[o];
		let rule = this.playerData.rules[o];
		let name = this.playerData.names[o];
		let points = this.playerData.points[o];

		this.sketch.fill(color);
		this.sketch.rect(x, y + i * (size + spacing) - (size + spacing), size, size);

		let ruleText = "(" + rule + ")";
		this.sketch.textSize(size);
		this.sketch.fill(0);
		this.sketch.text(ruleText + " " + name + " -> " + points, x + size + spacing, y + i * (size + spacing), 200);
		i++;
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
	this.simulation.transferCells();
	this.simulationSteps++;
  };

  /**
   Renders cells.
   */
  _render = () =>  {
	this._renderSelectedShape();
	//actualCells.forEach((c) => c.render(getViewport()));
	this.simulation.render(this._getViewport());
  };

  _renderSelectedShape = () => {
	if(!this.selectedShape) {
	  return;
	}

	let shape = this.selectedShape.shape;
	let indices = this._findIndices(shape.filter((p) => p.bit === "1").map((p) => {
	  return {x: p.x, y: p.y}
	}));

	let positions = this._findPositions(indices);
	for(let i = 0; i < positions.length; i++) {
	  let p = positions[i];
	  Cell.rectRenderFunction(
	    p.x + (1 - this.cellSize) * 0.5, p.y + (1 - this.cellSize) * 0.5,
		this.cellSize, this.cellSize, "#adeedd", this.sketch);
	}
  };


  _findPositions = (indices) => {
	return indices.map((i) => {
	  let x = i % this.width;
	  let y = Math.floor(i / this.width);
	  return {x: x * this.cellSize, y: y * this.cellSize};
	});
  };

  _findIndices = (positions) => {
	let indices = [];
	for(let i = 0; i < positions.length; i++) {
	  indices.push(this._getIndexMouseWithOffset(positions[i].x, positions[i].y));
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
  };

  _onPlayerData = (msg) => {
	this.playerData = msg;
	this.simulation.setPlayerData(msg);
  };

  _onMapData = (data) => {
	this.width = data.width;
	this.height = data.height;
	this.simulation = new Simulation(this.width, this.height, this.playerData, this.cellSize, this.rectRenderFunction);
  };

  _onMapUpdate = (data) => {
	this.connected = true;
	let newCells = data.cells;
	if (newCells.length === this.width * this.height) {
	  this.actualCells = [];
	  console.log("Received all data.");
	  for (let i = 0; i < newCells.length; i++) {
		let newCell = newCells[i];
		let {x, y, alive, ownerId} = newCell;
		let cell = new Cell(x, y, alive, ownerId, this.cellSize, "#0000ff", Cell.rectRenderFunction, this.sketch);
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

  _onTickData = (data) => {
	if (this.simulationSteps === -1) {
	  this.simulationSteps = data.simulationSteps;
	}
	let tick = data.simulationSteps;
	let difference = tick - this.simulationSteps;
	console.log("Sent tick " + tick + ". Client side ticks " + this.simulationSteps);
	this._advanceSimulation();
	if (difference > 0) {
	  //advanceSimulation();
	  console.log("Tick difference of " + difference + ", advancing.");
	}
  };

  getFPS = () => {
    return this.FPS;
  };

  rectRenderFunction = (color, x, y, width, height) => {
	this.sketch.fill(color);
	this.sketch.rect(x, y, width, height);
  };

  ellipseRenderFunction = (color, x, y, width, height) => {
	this.sketch.fill(color);
	this.sketch.ellipse(x + width / 2, y + height / 2, width, height);
  };

}

let sketch = new p5((p) => {

  let game;

  p.setup = () => {
	game = new Game(p.createCanvas(window.innerWidth, window.innerHeight), p);
	p.frameRate(game.getFPS());
  };

  p.keyPressed = (event) => {
	if(game.connected) {
	  game.selectedShape = game.shapeMap[event.keyCode]; //TODO FIX
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
  };

});

