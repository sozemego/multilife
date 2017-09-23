import {cellCreator as createCell} from "./cell";
import { basicRule } from "./rules";
import {convertIntToHexColor, throwError} from "./utils";

const validateConstructorArguments = (width, height, playerData) => {
	if(typeof width !== "number") {
		throwError("width is not a number, it is: " + width);
	}
	if(typeof height !== "number") {
		throwError("height is not a number, it is: " + height);
	}
	if(!playerData) {
		throwError("playerData cannot be null or undefined");
	}
};

const mode = ownerIds => {
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

/**
 * Object responsible for the simulation of game of life.
 */
export const createSimulation = (width, height, playerData) => {
	validateConstructorArguments(width, height, playerData);

	const cells = {};
	let activeCells = {};
	let nextCells = {};
	const defaultOwnerId = 0;

	const simulation = {};

	let initialized = false;

	/**
	 * Creates all cells.
	 */
	simulation.init = () => {
		for (let i = 0; i < width; i++) {
			for (let j = 0; j < height; j++) {
				cells["x:" + i + "y:" + j] =
					createCell(
						i, j, false, defaultOwnerId,
						getColor(defaultOwnerId)
					);
			}
		}
		initialized = true;
	};

	/**
	 * Sets the state of a cell at a given position. This does not update
	 * the current cell state, but next cell state (will be used in the next iteration).
	 * @param position
	 * @param alive
	 * @param ownerId
	 */
	simulation.setCellState = (position, alive, ownerId) => {
		validateInitialized();
		let cell = nextCells[getPositionKey(position)];
		if (!cell) {
			cell = createCell(
				position.x, position.y, alive, ownerId, getColor(ownerId),
			);
			nextCells[getPositionKey(position)] = cell;
		}
	};

	simulation.update = () => {
		validateInitialized();
		for (const pos in activeCells) {
			if (activeCells.hasOwnProperty(pos)) {
				const cell = activeCells[pos];
				const {x, y} = cell.getPosition();
				const aliveNeighbours = getAliveNeighbourCells(x, y);
				const state = basicRule(aliveNeighbours.length, cell.isAlive());
				if (state !== 0) {
					const strongestOwnerId = getStrongestOwnerId(aliveNeighbours);
					simulation.setCellState(
						{x, y},
						state > 0,
						strongestOwnerId == -1 ? cell.getOwnerId() : strongestOwnerId
					);
				}
			}
		}
		transferCells();
	};

	simulation.render = viewport => {
		validateInitialized();
		for (const pos in cells) {
			if (cells.hasOwnProperty(pos)) {
				cells[pos].update();
				cells[pos].render(viewport);
			}
		}
	};

	simulation.setPlayerData = newPlayerData => {
		Object.assign(playerData, newPlayerData);
	};

	const validateInitialized = () => {
		if(!initialized) {
			throwError("Simulation is not initialized!");
		}
	};

	const transferCells = () => {
		activeCells = {};
		for (const pos in nextCells) {
			if (nextCells.hasOwnProperty(pos)) {
				const oldCell = nextCells[pos];
				const c = cells[pos];
				c.setAlive(oldCell.isAlive());
				c.setOwnerId(oldCell.getOwnerId());
				c.setColor(getColor(oldCell.getOwnerId()));
				addToActive(c);
			}
		}
		nextCells = {};
	};

	/**
	 * Transforms the position of a cell into the key used in maps
	 * containing the cells. The position is wrapped around the grid.
	 * @param position
	 * @returns {string}
	 * @private
	 */
	const getPositionKey = ({x, y})=> {
		let index = x + (y * width); // find index
		const maxSize = width * height;
		if (index < 0) index = index + (maxSize); // wrap around if neccesary
		if (index >= maxSize) index = index % (maxSize);

		const wrappedX = index % width;
		const wrappedY = Math.floor(index / width);

		return "x:" + wrappedX + "y:" + wrappedY;
	};

	const getColor = ownerId => {
		if(playerData[ownerId]) {
			const color = playerData[ownerId].color;
			return color ? convertIntToHexColor(color) : "#000000";
		}
		return "#000000";
	};

	/**
	 Returns an array of alive cells neighbouring a cell at x, y.
	 */
	const getAliveNeighbourCells = (x, y) => {
		const aliveCells = [];
		for (let i = -1; i < 2; i++) {
			for (let j = -1; j < 2; j++) {
				if (i === 0 && j === 0) continue;
				const positionKey = getPositionKey({x: i + x, y: j + y});
				const cell = cells[positionKey];
				if (cell.isAlive()) aliveCells.push(cell);
			}
		}
		return aliveCells;
	};

	const getStrongestOwnerId = cells => {
		if (cells.length === 0) {
			return -1;
		}

		const ownerIds = cells.map((cell) => {
			return cell.getOwnerId();
		});

		return mode(ownerIds);
	};

	/**
	 * Takes a cell and adds its to active cells. This cell and its neighbours
	 * are added to active cells.
	 * @param cell
	 * @private
	 */
	const addToActive = cell => {
		const {x, y} = cell.getPosition();
		for (let i = -1; i < 2; i++) {
			for (let j = -1; j < 2; j++) {
				const position = getPositionKey({x: i + x, y: j + y});
				const cell = activeCells[position];
				if (!cell) {
					activeCells[position] = cells[position];
				}
			}
		}
	};

	return simulation;
};
