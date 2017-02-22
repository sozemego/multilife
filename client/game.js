/**
 * Created by KJurek on 16.02.2017.
 */

/**
 Define global variables for this game.
 */
let cells = [];
let canvas = document.getElementById("canvas");
let ctx = canvas.getContext("2d");
let width = 0;
let height = 0;
let cellSize = 5;
let ws;
let clicked = false;
let myId = 0;
let playerColors;
let intervalId;

function getLoginObject(name) {
    return {type: "LOGIN", name: name, rule: "BASIC"};
}

document.addEventListener("mousedown", function(event) {
    clicked = true;
});

document.addEventListener("mouseup", function(event) {
    clicked = false;
});

document.addEventListener("mousemove", function(event) {
    onMouseMove(event.pageX, event.pageY);
});

document.getElementById("login-button").addEventListener("click", function(event) {
	let inputField = document.getElementById("name");
	let name = inputField.value;
	login(name);
});

/**
 Bind event handler for when a player chooses a name
 (so, attempts to login).
 */
const nameInput = document.getElementById("name");
nameInput.addEventListener("keydown", function(event) {
    this.onName(event);
}.bind(this));

/**
 Fired when user inputs something in the name field.
 On enter press, attempts to login the user.
 */
function onName(event) {
    if(event.keyCode === 13) {
        login(event.target.value);
    }
}

/**
  Called when user sends submit name, instead of pressing return
  in the input field itself.
*/
function onNameSubmit(event) {
    let inputField = document.getElementById("name");
    let name = inputField.value;
    login(name);
}

/**
 Attempts to login with a given name
*/
function login(name) {
  ws = new WebSocket("ws://127.0.0.1:8080/game");
  ws.onopen = function() {
  	ws.send(JSON.stringify(getLoginObject(name)));
  };

  ws.onmessage = function(msg) {
    handleMessage(JSON.parse(msg.data));
  };

	document.getElementById("login").classList.toggle("logged");
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
    console.log("Received message of type", msg.type);
}

function onMapData(data) {
    cells = [];
    width = data.width;
    height = data.height;
    playerColors = data.playerColors;
		play();
}

function calculateCellSize() {
	if(width > height) {
			cellSize = canvas.width / width;
	}
	if(height >= width) {
			cellSize = canvas.height / height;
	}
}

function onMapUpdate(data) {
    cells = [];
    let newCells = data.cells;
    for(let i = 0; i < newCells.length; i++) {
        let cell = newCells[i];
        cells.push(cell);
    }
}

function play() {
		clearInterval(intervalId);
    intervalId = setInterval(render, 16);
}

function render() {
    canvas.width  = window.innerWidth;
    canvas.height = window.innerHeight;
		calculateCellSize();
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    for(let i = 0; i < cells.length; i++) {
        let cell = cells[i];
        drawCell(cell, i);
    }
}

function drawCell(cell, i) {
		if(!cell.alive) {
			return;
		}
    ctx.beginPath();
    ctx.fillStyle = getColor(cell.ownerId);
    let position = getPosition(i);
    ctx.rect(position.x * cellSize, position.y * cellSize, cellSize, cellSize);
    ctx.fill();
}

function getColor(id) {
    return playerColors[id] || "#000000";
}

function getPosition(i) {
    return {x: i % width, y: Math.floor(i / width)};
}

function onMouseMove(x, y) {
    if(clicked === false) {
        return;
    }
    if(ws) {
        let canvasPosition = getCanvasPosition(x, y);

        if(canvasPosition.x < 0 || canvasPosition.x > canvas.width) {
            return;
        }
        if(canvasPosition.y < 0 || canvasPosition.y > canvas.height) {
            return;
        }

				let indices = [];
				for(let i = -4; i < 4; i++) {
					for(let j = -4; j < 4; j++) {
						let index = getIndex(canvasPosition.x + (i * cellSize), canvasPosition.y + (j * cellSize));
						if(!cells[index].alive) {
							cells[index].alive = true;
							cells[index].ownerId = myId;
						}
						indices.push(index);
					}
				}

        ws.send(JSON.stringify({type:"CLICK", indices: indices}));
				render();
    }
}

function getCanvasPosition(x, y) {
    let rect = canvas.getBoundingClientRect();
    return {
        x: x - rect.left,
        y: y - rect.top
    };
}

function getIndex(x, y) {
    x = x / cellSize;
    y = y / cellSize;
    return Math.floor(x) + (Math.floor(y) * width);
}
