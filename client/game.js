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
let clicked = false;

function setup() {
	canvas = createCanvas(600, 600);

	input = createInput();
	input.position(300, 300);

	button = createButton("enter");
	button.position(300, 350);
	button.mousePressed(login);

	canvas.mousePressed(click);
	canvas.mouseReleased(release);
	canvas.mouseMoved(onMouseMove);

}

function draw() {
	background(245);
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
	clicked = false;
}

/**
	Called when a mouse is moved.
*/
function onMouseMove() {
	if(!clicked) {
		return;
	}
	if(ws) {
		let indices = [];
		for(let i = -4; i < 4; i++) {
			for(let j = -4; j < 4; j++) {
				let index = getIndex(mouseX + (i * cellSize), mouseY + (j * cellSize));
				if(!cells[index].alive) {
					cells[index].alive = true;
					cells[index].ownerId = myId;
				}
				indices.push(index);
			}
		}
		ws.send(JSON.stringify({type:"CLICK", indices: indices}));
		//render();
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
	Returns color of the player with a given id.
*/
function getColor(id) {
    return playerColors[id] || "#000000";
}

function render() {
    for(let i = 0; i < cells.length; i++) {
        let cell = cells[i];
        drawCell(cell, i);
    }
}

function drawCell(cell, i) {
		if(!cell.alive) {
			return;
		}
		let color = getColor(cell.ownerId);
		fill(color);
		rect(cell.x * cellSize, cell.y * cellSize, cellSize, cellSize);
}


function handleMessage(msg) {
    if(msg.type === "PLAYER_IDENTITY") {
        myId = msg.playerId;
    }
    if(msg.type === "MAP_DATA") {
        onMapData(msg);
    }
    if(msg.type === "CELL_LIST") {
        onMapUpdate(msg);
    }
    //console.log("Received message of type", msg.type);
}

function onMapData(data) {
    let oldCells = cells.splice();
    width = data.width;
    height = data.height;
		canvas.size(width * cellSize, height * cellSize);

    for(let i = 0; i < height; i++) {
        for(let j = 0; j < width; j++) {
            cells.push({x: j, y: i});
        }
    }
    playerColors = data.playerColors;
}

function onMapUpdate(data) {
    //console.log(data.cells.length);
    let newCells = data.cells;

    for(let i = 0; i < newCells.length; i++) {
        let newCell = newCells[i];
        let index = getIndex(newCell.x * cellSize, newCell.y * cellSize);
        cells[index] = newCell;
    }
}
