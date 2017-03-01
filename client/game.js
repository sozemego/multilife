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

/**
	A NullObject simulation.
*/
let simulation = {
	update: function(){},
	render: function(){},
	transferCells: function(){},
	setPlayerData: function(){}
};

let ticks = 0;
let FPS = 60;
let stepsPerSecond = 4;
let stepPerFrames = FPS / stepsPerSecond;
let simulationSteps = 0;

let playerData;

function setup() {
	canvas = createCanvas(600, 600);
	frameRate(FPS);

	input = createInput();
	input.position(300, 300);

	button = createButton("enter");
	button.position(300, 350);
	button.mousePressed(login);

	canvas.mousePressed(click);
	canvas.mouseReleased(release);

}

function draw() {
	background(245);
	ticks++;
	update();
	render();
}

/**
 Attempts to login with a given name
*/
function login() {
	let name = input.value();
	if(!name) {
		return;
	}
	input.remove();
	button.remove();

  ws = new WebSocket("ws://127.0.0.1:8080/game");
  ws.onopen = function() {
  	ws.send(JSON.stringify(getLoginObject(name)));
  };

  ws.onmessage = function(msg) {
    handleMessage(JSON.parse(msg.data));
  };
}

function getLoginObject(name) {
    return {type: "LOGIN", name: name, rule: "BASIC"};
}

function click() {
	clicked = true;
}

function release() {
    if(recentlyClicked) {
        return;
    }
	if(ws) {
    	let indices = [];
    	for(let i = -4; i < 4; i++) {
    		for(let j = -4; j < 4; j++) {
    			let index = getIndex(mouseX + (i * cellSize), mouseY + (j * cellSize));
    			indices.push(index);
    		}
    	}
    	ws.send(JSON.stringify({type:"CLICK", indices: indices}));
    	recentlyClicked = true;
    }
}

/**
	Returns a index in the cell array of a point at location x, y.
	x, y represents a point on a canvas (in pixels).
*/
function getIndex(x, y) {
    x = x / cellSize;
    y = y / cellSize;
    let index = Math.floor(x) + (Math.floor(y) * width);
    let maxSize = width * height;
    if(index < 0) return index + (maxSize); // wrap around if neccesary
    if(index >= maxSize) return index % (maxSize);
    return index; // return index if not neccesary to wrap
}

/**
	Updates the simulation.
*/
function update() {
	if(!connected) {
		return;
	}
	if(ticks % stepPerFrames === 0) {
    	simulation.update();
		simulation.transferCells();
		recentlyClicked = false;
		simulationSteps++;
	}
}

/**
	Renders cells.
*/
function render() {
	simulation.render();
}

function handleMessage(msg) {
    if(msg.type === "PLAYER_IDENTITY") {
        myId = msg.playerId;
    }
    if(msg.type === "PLAYER_DATA") {
        onPlayerData(msg);
    }
    if(msg.type === "MAP_DATA") {
        onMapData(msg);
    }
    if(msg.type === "CELL_LIST") {
        onMapUpdate(msg);
    }
    if(msg.type === "TICK_DATA") {
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
    canvas.size(width * cellSize, height * cellSize);
    simulation = new Simulation(width, height, playerData);
}

function onMapUpdate(data) {
	connected = true;
    let newCells = data.cells;
    for(let i = 0; i < newCells.length; i++) {
        let newCell = newCells[i];
        simulation.setCellState({x: newCell.x, y: newCell.y}, newCell.alive, newCell.ownerId);
    }
}

function onTickData(data) {
	let tick = data.simulationSteps;
	console.log("Sent tick " + tick + ". Client side ticks " + simulationSteps);
}