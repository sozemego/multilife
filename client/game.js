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
let simulationSteps;
let wait = false;

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
	canvas.size(Math.max(window.innerWidth, width * cellSize), Math.max(window.innerHeight, height * cellSize));
	background(245);
	ticks++;
	renderPlayerPoints();
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
    return {type: "LOGIN", name: name, rule: getRandomRule()};
}

function getRandomRule() {
    let rules = ["BASIC", "HIGHLIFE", "REPLICATOR",
     "NO_NAME", "DIAMOEBA", "MORLEY", "FOUR",
      "CORAL", "LIFE_34", "SEEDS", "ANNEAL"];
      return rules[getRandomInt(0, rules.length - 1)];
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
    	for(let i = -4; i < 5; i++) {
    		for(let j = -4; j < 5; j++) {
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
	if(x > cellSize * width || y > cellSize * height) {
		return;
	}
    x = x / cellSize;
    y = y / cellSize;
    let index = Math.floor(x) + (Math.floor(y) * width);
    let maxSize = width * height;
    if(index < 0) return index + (maxSize); // wrap around if neccesary
    if(index >= maxSize) return index % (maxSize);
    return index; // return index if not neccesary to wrap
}

function renderPlayerPoints() {
	if(!playerData) {
		return;
	}
	let x = window.scrollX + canvas.width - 400;
	let y = 50 + window.scrollY;
	let size = 17;
	let spacing = 4;

	let i = 0;
	for(let o in playerData.colors) {
		if(playerData.colors.hasOwnProperty(o)) {
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
	if(!connected) {
		return;
	}
	if(ticks % stepPerFrames === 0) {
    	advanceSimulation();
		recentlyClicked = false;
	}
}

function advanceSimulation() {
	if(wait) {
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
    let viewport = {
        x: window.scrollX,
        y: window.scrollY,
        width: window.innerWidth,
        height: window.innerHeight
    };
	simulation.render(viewport);
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
    if(simulation instanceof Simulation) {
        return;
    }
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
	wait = false;
	if(simulationSteps === undefined || isNaN(simulationSteps)) {
		simulationSteps = data.simulationSteps;
	}
	let tick = data.simulationSteps;
	let difference = tick - simulationSteps;
	console.log("Sent tick " + tick + ". Client side ticks " + simulationSteps);
	if(difference > 0) {
		advanceSimulation();
		console.log("Tick difference of " + difference + ", advancing.");
	} else if (difference < 0) {
		 wait = true;
	}
}

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}