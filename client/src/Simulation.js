import Cell from "./Cell";
import Rules from "./Rules";

export default class Simulation {

  constructor(width, height, playerData, cellSize, sketch) {
	this.width = width;
	this.height = height;
	this.width = width;
	this.height = height;
	this.cells = {};
	this.activeCells = {};
	this.nextCells = {};
	this.playerData = playerData;
	this.cellSize = cellSize;
	this.rules = new Rules();
	this.sketch = sketch;
	this._init();
  }

  /**
   * Creates all cells.
   */
  _init = () => {
	for(let i = 0; i < this.width; i++) {
	  for(let j = 0; j < this.height; j++) {
		this.cells["x:" + i + "y:" + j] = new Cell(i, j, false, 0, this.cellSize, this._getColor(0), Cell.ellipseRenderFunction, this.sketch);
	  }
	}
  };

  setCellState = (position, alive, ownerId) => {
	let cell = this.nextCells[this._getPositionKey(position)];
	if(!cell) {
	  cell = new Cell(position.x, position.y, alive, ownerId, this.cellSize, this._getColor(ownerId), Cell.ellipseRenderFunction, this.sketch);
	  this.nextCells[this._getPositionKey(position)] = cell;
	}
  };

  /**
   Transforms the position of a cell into the key used in maps
   containing the cells.
   */
  _getPositionKey = (position) => {
	let {x, y} = position;
	let index = x + (y * this.width); // find index
	let maxSize = this.width * this.height;
	if (index < 0) index = index + (maxSize); // wrap around if neccesary
	if (index >= maxSize) index = index % (maxSize);

	let wrappedX = index % this.width;
	let wrappedY = Math.floor(index / this.width);

	return "x:" + wrappedX + "y:" + wrappedY;
  };

  _getColor = (ownerId) => {
	return this.playerData.colors[ownerId] || "#000000";
  };

  update = () => {
	for(let pos in this.activeCells) {
	  if (this.activeCells.hasOwnProperty(pos)) {
		let cell = this.activeCells[pos];
		let x = cell.x;
		let y = cell.y;
		let aliveNeighbours = this._getAliveNeighbourCells(x, y);
		let ownerId = cell.getOwnerId();
		let state = this.rules.getRule(this.playerData.rules[ownerId])(aliveNeighbours.length, cell.isAlive());
		if (state != 0) {
		  let strongestOwnerId = this._getStrongestOwnerId(aliveNeighbours);
		  this.setCellState({x:x, y:y}, state > 0, strongestOwnerId == -1 ? cell.getOwnerId() : strongestOwnerId);
		}
	  }
	}
  };

  /**
   Returns an array of alive cells neighbouring a cell at x, y.
   */
  _getAliveNeighbourCells = (x, y) => {
	let cells = [];
	for(let i = -1; i < 2; i++) {
	  for(let j = -1; j < 2; j++) {
		if(i === 0 && j === 0) continue;
		let positionKey = this._getPositionKey({x: i + x, y: j + y});
		let cell = this.cells[positionKey];
		if(cell.isAlive()) cells.push(cell);
	  }
	}
	return cells;
  };

  _getStrongestOwnerId = (cells) => {
	if(cells.length === 0) {
	  return -1;
	}

	let ownerIds = cells.map((cell) => {
	  return cell.getOwnerId();
	});

	return this._mode(ownerIds);
  };

  _mode = (ownerIds) => {
	let maxValue = 0, maxCount = 0;

	for (let i = 0; i < ownerIds.length; ++i) {
	  let count = 0;
	  for (let j = 0; j < ownerIds.length; ++j) {
		if (ownerIds[j] === ownerIds[i]) ++count;
	  }
	  if (count > maxCount) {
		maxCount = count;
		maxValue = ownerIds[i];
	  }
	}
	return maxValue;
  };

  transferCells = () => {
	this.activeCells = {};
	for(let pos in this.nextCells) {
	  if(this.nextCells.hasOwnProperty(pos)) {
		let oldCell = this.nextCells[pos];
		let c = this.cells[pos];
		c.setAlive(oldCell.isAlive());
		c.setOwnerId(oldCell.getOwnerId());
		c.setColor(this._getColor(oldCell.getOwnerId()));
		this._addToActive(c);
	  }
	}
	this.nextCells = {};
  };

  _addToActive = (cell) => {
	let x = cell.x;
	let y = cell.y;
	for(let i = -1; i < 2; i++) {
	  for(let j = -1; j < 2; j++) {
		let position = this._getPositionKey({x: i + x, y: j + y});
		let cell = this.activeCells[position];
		if(!cell) {
		  this.activeCells[position] = this.cells[position];
		}
	  }
	}
  };

  render = (viewport) => {
	for(let pos in this.cells) {
	  if(this.cells.hasOwnProperty(pos)) {
		this.cells[pos].update();
		this.cells[pos].render(viewport);
	  }
	}
  };

  setPlayerData = (playerData) => {
	this._removeDisconnectedPlayers(playerData);
	Object.assign(this.playerData, playerData);
  };

  _removeDisconnectedPlayers = (newPlayerData) => {
	let oldData = this.playerData;
	let oldPlayerIds = this._getPlayerIds(oldData.rules);
	let newPlayerIds = this._getPlayerIds(newPlayerData.rules);
	let disconnectedPlayers = this._findDisconnectedPlayers(oldPlayerIds, newPlayerIds);
	for(let i = 0; i < disconnectedPlayers.length; i++) {
	  let id = disconnectedPlayers[i];
	  for(let pos in this.cells) {
		if(this.cells.hasOwnProperty(pos)) {
		  let cell = this.cells[pos];
		  if(cell.ownerId == id) {
			cell.setAlive(false);
			cell.setOwnerId(0);
		  }
		}
	  }
	}
  };

  _getPlayerIds = (playerData) => {
	let arr = [];
	for(let id in playerData) {
	  if(playerData.hasOwnProperty(id)) {
		arr.push(id);
	  }
	}
	return arr;
  };

  _findDisconnectedPlayers = (oldPlayerIds, newPlayerIds) => {
	let disconnectedPlayers = [];
	for(let i = 0; i < oldPlayerIds.length; i++) {
	  let oldPlayerId = oldPlayerIds[i];
	  let index = newPlayerIds.findIndex((i) => i === oldPlayerId);
	  if(index === -1) {
		disconnectedPlayers.push(oldPlayerId);
	  }
	}
	return disconnectedPlayers;
  }

}
