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

function getLoginObject(name) {
    return {type: "LOGIN", name: name, rule: "LIFE_WITHOUT_DEATH"};
}

document.addEventListener("mousedown", function(event) {
    clicked = true;
});

document.addEventListener("mouseup", function(event) {
    clicked = false;
});

document.addEventListener("mousemove", function(event) {
    onClick(event.pageX, event.pageY);
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
        ws = new WebSocket("ws://127.0.0.1:8080/game");
        ws.onopen = function() {
            ws.send(JSON.stringify(getLoginObject(event.target.value)));
        };

        ws.onmessage = function(msg) {
            handleMessage(JSON.parse(msg.data));
        };

        document.getElementById("login").classList.toggle("logged");
    }
}

function handleMessage(msg) {
    if(msg.type === "PLAYER_IDENTITY") {
        myId = msg.id;
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
    if(width > height) {
        cellSize = canvas.width / width;
    }
    if(height >= width) {
        cellSize = canvas.height / height;
    }
    playerColors = data.playerColors;
}

function onMapUpdate(data) {
    cells = [];
    let newCells = data.cells;
    for(let i = 0; i < newCells.length; i++) {
        let cell = newCells[i];
        cells.push(cell);
    }
    render();
}

function play() {
    render();
}

function render() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    for(let i = 0; i < cells.length; i++) {
        let cell = cells[i];
        drawCell(cell, i);
    }
}

function drawCell(cell, i) {
    let alive = cell.alive;
    ctx.beginPath();
    ctx.fillStyle = alive ? getColor(cell.ownerId) : "#ffffff";
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

function onClick(x, y) {
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

        let index = getIndex(canvasPosition.x, canvasPosition.y);
        cells[index].alive = true;
        ws.send(JSON.stringify({type:"CLICK", index: index}));
        console.log(index);
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
