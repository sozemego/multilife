import p5 from "p5";
import Simulation from "./Simulation";
import Cell from "./cell";

// let rules = ["BASIC", "HIGHLIFE", "REPLICATOR",
//   "NO_NAME", "MORLEY", "FOUR", "CORAL",
//   "LIFE_34", "SEEDS", "ANNEAL"];

class Game {

  constructor(canvas, sketch) {
    this.sketch = sketch;
	this.ticks = 0;
	this.FPS = 30;
	this.stepsPerSecond = 4;
	this.stepPerFrames = this.FPS / this.stepsPerSecond;
	this.simulationSteps;
	this.wait = false;
    this.canvas = canvas;
	this.canvas.mouseReleased(this._release);
	this.simulation = new Simulation(0, 0, {});
  	this.cells = [];
  	this.width = 0;
  	this.height = 0;
  	this.cellSize = 10;
  	this.webSocket = undefined;
  	this.myId = 0;
  	this.playerColors = {};
  	this.rules = ["BASIC"];
	this.connected = false;
	this.clicked = false;
	this.recentlyClicked = false;
	this.playerData = {};
	this.actualCells = [];
	this.firstMapData = true;
	this._initShapes();
	this._createLoginView();
  }

  _release = () => {
	if (this.recentlyClicked) {
	  return;
	}

	if (this.ws) {
	  this._sendShape(this.selectedShape.shape);
	  this.recentlyClicked = true;
	}
  };

  _sendShape = (shape) => {
	if(!shape) {
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
	this.ws.send(JSON.stringify({type: "CLICK", indices: indices}));
  };

  _initShapes = () => {
    let shapeMap = this.shapeMap = {};
	// INIT BASIC RULES
	shapeMap[81] = {name: "block", shape: this._parseShape("11,11")};
	shapeMap[87] = {name: "hive", shape: this._parseShape("0110,1001,0110")};
	shapeMap[69] = {name: "loaf", shape: this._parseShape("0010,0101,1001,0110")};
	shapeMap[82] = {name: "tub", shape: this._parseShape("010,101,010")};
	shapeMap[65] = {name: "blinker", shape: this._parseShape("1,1,1")};
	shapeMap[83] = {name: "floodgate", shape: this._parseShape("1110000,0100000,0000000,0000000,0000001,0000011,0000001")};
  };

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
	dom.appendChild(name);

	let buttonRow = document.createElement("div");
	dom.appendChild(buttonRow);
	for (let i = 0; i < this.rules.length; i++) {
	  let button = document.createElement("button");
	  button.appendChild(document.createTextNode(this.rules[i]));
	  button.setAttribute("val", this.rules[i]);
	  button.addEventListener("click", (event) => {
		this._login(event.target.getAttribute("val"));
	  });
	  buttonRow.appendChild(button);
	}
  };

  _login = (rule) => {
	let name = document.getElementById("name").value;
	if (!name) {
	  return;
	}

	this.webSocket = new WebSocket("ws://127.0.0.1:8080/game");
	this.webSocket.onopen = () => {
	  this.ws.send(JSON.stringify({type: "LOGIN", name: name, rule: rule}));
	  document.getElementById("login").classList.add("logged");
	};

	this.webSocket.onmessage = (msg) => {
	  this._handleMessage(JSON.parse(msg.data));
	};
  };

  _getIndexMouseWithOffset = (xOffset, yOffset) => {
	return this._getIndex(mouseX + (xOffset * this.cellSize), mouseY + (yOffset * this.cellSize));
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
    let c = this.canvas.canvas;
    c.width = Math.max(window.innerWidth, this.width * this.cellSize);
    c.height = Math.max(window.innerHeight, this.height * this.cellSize);
    this.sketch.background(245);
	this.ticks++;
	this._renderPlayerPoints();
	this._update();
	this._render();
  };

  _renderPlayerPoints = () => {
	if (!this.playerData) {
	  return;
	}
	let x = window.scrollX + this.canvas.width - 400;
	let y = 50 + window.scrollY;
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
		this.sketch.text(ruleText + " " + name + " -> " + points, x + size + spacing, y + i * (size + spacing));
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
	if (this.wait) {
	  return;
	}
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
	fill("#adeedd");
	for(let i = 0; i < positions.length; i++) {
	  let p = positions[i];
	  Cell.rectRenderFunction(p.x + (1 - this.cellSize) * 0.5, p.y + (1 - this.cellSize) * 0.5, this.cellSize, this.cellSize);
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
	this.simulation = new Simulation(this.width, this.height, this.playerData);
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
		let cell = new Cell(x, y, alive, ownerId, this.cellSize, "#0000ff", Cell.rectRenderFunction);
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
	this.wait = false;
	if (this.simulationSteps === undefined || isNaN(this.simulationSteps)) {
	  this.simulationSteps = data.simulationSteps;
	}
	let tick = data.simulationSteps;
	let difference = tick - this.simulationSteps;
	console.log("Sent tick " + tick + ". Client side ticks " + this.simulationSteps);
	this._advanceSimulation();
	if (difference > 0) {
	  //advanceSimulation();
	  console.log("Tick difference of " + difference + ", advancing.");
	} else if (difference < 0) {
	  this.wait = true;
	}
  };

  getFPS = () => {
    return this.FPS;
  };

}

let sketch = new p5((p) => {

  let game;

  p.setup = () => {
	game = new Game(p.createCanvas(600, 600), p);
	p.frameRate(game.getFPS());
  };

  p.keyPressed = (event) => {
	if(game.connected) {
	  game.selectedShape = shapeMap[event.keyCode]; //TODO FIX
	}
  };

  p.draw = () => {
	game.draw();
  };

});

