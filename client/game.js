let cells = [];
let width = 0;
let height = 0;
let cellSize = 10;
let ws;
let myId = 0;
let playerColors;

let canvas;

let input;
let button;
let connected = false;
let clicked = false;
let recentlyClicked = false;

let rules = ["BASIC", "HIGHLIFE", "REPLICATOR",
  "NO_NAME", "MORLEY", "FOUR", "CORAL",
  "LIFE_34", "SEEDS", "ANNEAL"];

/**
 A NullObject simulation.
 */
let simulation = {
  update: function () {},
  render: function () {},
  transferCells: function () {},
  setPlayerData: function () {}
};

let ticks = 0;
let FPS = 1;
let stepsPerSecond = 40;
let stepPerFrames = FPS / stepsPerSecond;
let simulationSteps;
let wait = false;

let playerData;

let actualCells = [];

let firstMapData = true;

function setup() {
  canvas = createCanvas(600, 600);
  frameRate(FPS);
  createLoginView();
  canvas.mousePressed(click);
  canvas.mouseReleased(release);
}

function createLoginView() {
	let dom = document.getElementById("login");

	let name = document.createElement("input");
	name.setAttribute("id", "name");
	dom.appendChild(name);

	let buttonRow = document.createElement("div");
	dom.appendChild(buttonRow);
	for(let i = 0; i < rules.length; i++) {
	  let button = document.createElement("button");
	  button.appendChild(document.createTextNode(rules[i]));
	  button.setAttribute("val", rules[i]);
	  button.addEventListener("click", (event) => {
		login(event.target.getAttribute("val"));
	  });
	  buttonRow.appendChild(button);
	}
}

function draw() {
  canvas.size(Math.max(window.innerWidth, width * cellSize), Math.max(window.innerHeight, height * cellSize));
  background(245);
  ticks++;
  renderPlayerPoints();
  update();
  render();
}

function login(rule) {
  let name = document.getElementById("name").value;
  if (!name) {
	return;
  }

  ws = new WebSocket("ws://127.0.0.1:8080/game");
  ws.onopen = function () {
	ws.send(JSON.stringify(getLoginObject(name, rule)));
	document.getElementById("login").classList.add("logged");
  };

  ws.onmessage = function (msg) {
	handleMessage(JSON.parse(msg.data));
  };
}

function getLoginObject(name, rule) {
  return {type: "LOGIN", name: name, rule: rule};
}

function getRandomRule() {
  return rules[getRandomInt(0, rules.length - 1)];
}

function click() {
  clicked = true;
}

function release() {
  if (recentlyClicked) {
	return;
  }
  if (ws) {
	let indices = [];
	for (let i = -4; i < 5; i++) {
	  for (let j = -4; j < 5; j++) {
		let index = getIndex(mouseX + (i * cellSize), mouseY + (j * cellSize));
		indices.push(index);
	  }
	}
	ws.send(JSON.stringify({type: "CLICK", indices: indices}));
	recentlyClicked = true;
  }
}

/**
 Returns a index in the cell array of a point at location x, y.
 x, y represents a point on a canvas (in pixels).
 */
function getIndex(x, y) {
  if (x > cellSize * width || y > cellSize * height) {
	return;
  }
  x = x / cellSize;
  y = y / cellSize;
  let index = Math.floor(x) + (Math.floor(y) * width);
  let maxSize = width * height;
  if (index < 0) return index + (maxSize); // wrap around if neccesary
  if (index >= maxSize) return index % (maxSize);
  return index; // return index if not neccesary to wrap
}

function renderPlayerPoints() {
  if (!playerData) {
	return;
  }
  let x = window.scrollX + canvas.width - 400;
  let y = 50 + window.scrollY;
  let size = 17;
  let spacing = 4;

  let i = 0;
  for (let o in playerData.colors) {
	if (playerData.colors.hasOwnProperty(o)) {
	  let color = playerData.colors[o];
	  let rule = playerData.rules[o];
	  let name = playerData.names[o];
	  let points = playerData.points[o];

	  fill(color);
	  rect(x, y + i * (size + spacing) - (size + spacing), size, size);

	  let ruleText = "(" + rule + ")";
	  textSize(size);
	  fill(0);
	  text(ruleText + " " + name + " -> " + points, x + size + spacing, y + i * (size + spacing));
	  i++;
	}
  }

}

/**
 Updates the simulation.
 */
function update() {
  if (!connected) {
	return;
  }
  if (ticks % stepPerFrames === 0) {
	//advanceSimulation();
	recentlyClicked = false;
  }
}

function advanceSimulation() {
  if (wait) {
	return;
  }
  simulation.update();
  simulation.transferCells();
  simulationSteps++;

}

/**
 Renders cells.
 */
function render() {
  actualCells.forEach((c) => c.render(getViewport()));
  simulation.render(getViewport());
}

function getViewport() {
  return {
	x: window.scrollX,
	y: window.scrollY,
	width: window.innerWidth,
	height: window.innerHeight
  };
}

function handleMessage(msg) {
  if (msg.type === "PLAYER_IDENTITY") {
	myId = msg.playerId;
  }
  if (msg.type === "PLAYER_DATA") {
	onPlayerData(msg);
  }
  if (msg.type === "MAP_DATA") {
	onMapData(msg);
  }
  if (msg.type === "CELL_LIST") {
	onMapUpdate(msg);
  }
  if (msg.type === "TICK_DATA") {
	onTickData(msg);
  }
}

function onPlayerData(msg) {
  playerData = msg;
  simulation.setPlayerData(msg);
}

function onMapData(data) {
  width = data.width;
  height = data.height;
  if (simulation instanceof Simulation) {
	return;
  }
  simulation = new Simulation(width, height, playerData);
}

function onMapUpdate(data) {
  connected = true;
  let newCells = data.cells;
  if(newCells.length === width * height) {
	actualCells = [];
    console.log("Received all data.");
	for (let i = 0; i < newCells.length; i++) {
	  let newCell = newCells[i];
	  let {x, y, alive, ownerId} = newCell;
	  let cell = new Cell(x, y, alive, ownerId, cellSize, "#0000ff", Cell.rectRenderFunction);
	  if(alive) {
	    cell.currentPercentageSize = 1;
	  }
	  actualCells.push(cell);
	}
	if(!firstMapData) {
	  return;
	}
  }
  for (let i = 0; i < newCells.length; i++) {
	let newCell = newCells[i];
	simulation.setCellState({x: newCell.x, y: newCell.y}, newCell.alive, newCell.ownerId);
  }
  if(firstMapData) {
    advanceSimulation();
    firstMapData = false;
  }
}

function onTickData(data) {
  wait = false;
  if (simulationSteps === undefined || isNaN(simulationSteps)) {
	simulationSteps = data.simulationSteps;
  }
  let tick = data.simulationSteps;
  let difference = tick - simulationSteps;
  console.log("Sent tick " + tick + ". Client side ticks " + simulationSteps);
  advanceSimulation();
  if (difference > 0) {
	//advanceSimulation();
	console.log("Tick difference of " + difference + ", advancing.");
  } else if (difference < 0) {
	wait = true;
  }
}

function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}