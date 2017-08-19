import Cell from "./Cell";
import { basicRule } from "./Rules";

/**
 * Class responsible for the simulation of game of life.
 */
export default class Simulation {

	constructor(width, height, playerData, cellSize, renderFunction) {
		this.width = width;
		this.height = height;
		this.cells = {};
		this.activeCells = {};
		this.nextCells = {};
		this.playerData = playerData;
		this.cellSize = cellSize;
		this.defaultOwnerId = 0;
		this.renderFunction = renderFunction;
		this._init();
	}

	/**
	 * Creates all cells.
	 */
	_init = () => {
		for (let i = 0; i < this.width; i++) {
			for (let j = 0; j < this.height; j++) {
				this.cells["x:" + i + "y:" + j] =
					new Cell(
						i, j, false, this.defaultOwnerId,
						this.cellSize, this._getColor(this.defaultOwnerId),
						this.renderFunction
					);
			}
		}
	};

	/**
	 * Sets the state of a cell at a given position. This does not update
	 * the current cell state, but next cell state (will be used in the next iteration).
	 * @param position
	 * @param alive
	 * @param ownerId
	 */
	setCellState = (position, alive, ownerId) => {
		let cell = this.nextCells[this._getPositionKey(position)];
		if (!cell) {
			cell = new Cell(
				position.x, position.y, alive, ownerId,
				this.cellSize, this._getColor(ownerId),
				this.renderFunction
			);
			this.nextCells[this._getPositionKey(position)] = cell;
		}
	};

	/**
	 * Transforms the position of a cell into the key used in maps
	 * containing the cells. The position is wrapped around the grid.
	 * @param position
	 * @returns {string}
	 * @private
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
		if(this.playerData[ownerId]) {
			const color = this.playerData[ownerId].color;
			return color ? this._convertIntToHexColor(color) : "#000000";
		}
		return "#000000";
	};

	_convertIntToHexColor = (int) => {
		int >>>= 0;
		const b = int & 0xFF,
			g = (int & 0xFF00) >>> 8,
			r = (int & 0xFF0000) >>> 16;
		return "rgb(" + [r, g, b].join(",") + ")";
	};

	update = () => {
		for (let pos in this.activeCells) {
			if (this.activeCells.hasOwnProperty(pos)) {
				let cell = this.activeCells[pos];
				let {x, y} = cell;
				let aliveNeighbours = this._getAliveNeighbourCells(x, y);
				let state = basicRule(aliveNeighbours.length, cell.isAlive());
				if (state !== 0) {
					let strongestOwnerId = this._getStrongestOwnerId(aliveNeighbours);
					this.setCellState({
						x: x,
						y: y
					}, state > 0, strongestOwnerId == -1 ? cell.getOwnerId() : strongestOwnerId);
				}
			}
		}
		this.transferCells();
	};

	/**
	 Returns an array of alive cells neighbouring a cell at x, y.
	 */
	_getAliveNeighbourCells = (x, y) => {
		let cells = [];
		for (let i = -1; i < 2; i++) {
			for (let j = -1; j < 2; j++) {
				if (i === 0 && j === 0) continue;
				let positionKey = this._getPositionKey({x: i + x, y: j + y});
				let cell = this.cells[positionKey];
				if (cell.isAlive()) cells.push(cell);
			}
		}
		return cells;
	};

	_getStrongestOwnerId = (cells) => {
		if (cells.length === 0) {
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
		for (let pos in this.nextCells) {
			if (this.nextCells.hasOwnProperty(pos)) {
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

	/**
	 * Takes a cell and adds its to active cells. This cell and its neighbours
	 * are added to active cells.
	 * @param cell
	 * @private
	 */
	_addToActive = (cell) => {
		let {x, y} = cell;
		for (let i = -1; i < 2; i++) {
			for (let j = -1; j < 2; j++) {
				let position = this._getPositionKey({x: i + x, y: j + y});
				let cell = this.activeCells[position];
				if (!cell) {
					this.activeCells[position] = this.cells[position];
				}
			}
		}
	};

	render = (viewport) => {
		for (let pos in this.cells) {
			if (this.cells.hasOwnProperty(pos)) {
				this.cells[pos].update();
				this.cells[pos].render(viewport);
			}
		}
	};

	setPlayerData = (playerData) => {
		Object.assign(this.playerData, playerData);
	};

}
