import {createSimulation} from "./simulation";
import {rectRenderFunction as renderFunction} from "./renderer";
import {
	CELL_LIST, MAP_DATA, PLACE_SHAPE, PLAYER_DATA_UPDATED, PLAYER_IDENTITY,
	PLAYER_REMOVED, SHAPE_PLACED, PLAYER_ADDED, PLAYER_POINTS,
	SHAPE_SELECTED, TICK_DATA, PLAYER_DATA
} from "./events";
import {notify, on} from "./event-bus";
import {sketch} from "./index";
import {assertIsArray, assertIsNumber, assertIsObject} from './assert';

const FPS = 60,
	cellSize = 10,
	playerData = {};

let simulation = createSimulation(0, 0, {}),
	width = 0,
	height = 0,
	recentlyClicked = false,
	simulationSteps = -1,
	framesRendered = 0,
	selectedShape = undefined,
	firstMapData = false;


/**
 * Returns index of a cell that is (xOffset, yOffset) away from mouse cursor.
 * xOffset and yOffset should be in pixels.
 * @param xPixels
 * @param yPixels
 */
const getIndexOffsetFromMouse = (xPixels, yPixels) => {
	assertIsNumber(xPixels);
	assertIsNumber(yPixels);
	return getIndex(
		sketch.mouseX + (xPixels * cellSize),
		sketch.mouseY + (yPixels * cellSize)
	);
};

/**
 * Returns a index in the cell array of a point at location x, y.
 * x, y represents a point on a canvas (in pixels).
 * @param x
 * @param y
 * @returns {number}
 */
const getIndex = (x, y) => {
	assertIsNumber(x);
	assertIsNumber(y);
	x = x / cellSize;
	y = y / cellSize;
	const index = Math.floor(x) + (Math.floor(y) * width);
	const maxSize = width * height;
	if (index < 0) return index + (maxSize); // wrap around if neccesary
	if (index >= maxSize) return index % (maxSize);
	return index; // return index if not neccesary to wrap
};

const advanceSimulation = () => {
	simulation.update();
	simulationSteps++;
};

/**
 Renders cells.
 */
const render = () => {
	renderSelectedShape();
	simulation.render(getViewport());
};

const renderSelectedShape = () => {
	if (!selectedShape) {
		return;
	}

	const shape = selectedShape.shape;
	const indices = findIndices(shape.filter((p) => p.bit === "1").map((p) => {
		return {x: p.x, y: p.y}
	}));

	const positions = findPositions(indices);
	for (let i = 0; i < positions.length; i++) {
		const p = positions[i];
		renderFunction(
			p.x + (1 - cellSize) * 0.5, p.y + (1 - cellSize) * 0.5,
			cellSize, cellSize, "#adeedd"
		);
	}
};

/**
 * Finds x,y positions of a given array of indices.
 * The indices are indices of a 1D array which represents
 * cells.
 * Returned positions are multiplied by cellSize, so they represent
 * coordinates were cell should be drawn.
 * @param indices
 * @returns {Number|*|Array}
 */
const findPositions = indices => {
	assertIsArray(indices);
	return indices.map((i) => {
		let x = i % width;
		let y = Math.floor(i / width);
		return {x: x * cellSize, y: y * cellSize};
	});
};

/**
 * Finds indices in 1D array of x, y positions.
 * The positions are translated by current mouse coordinates,
 * so they are positions in relation to the mouse cursor.
 * @param positions
 * @returns {Array}
 * @private
 */
const findIndices = positions => {
	assertIsArray(positions);
	const indices = [];
	for (let i = 0; i < positions.length; i++) {
		indices.push(getIndexOffsetFromMouse(positions[i].x, positions[i].y));
	}
	return indices;
};

const getViewport = () => {
	return {
		x: window.scrollX,
		y: window.scrollY,
		width: window.innerWidth,
		height: window.innerHeight
	};
};

const onShapeSelected = shape => {
	selectedShape = shape;
};

const onPlayerIdentity = ({playerId}) => {

};

const onPlayerData = msg => {
	assertIsObject(msg);
	simulation.setPlayerData(msg);
};

const onMapData = data => {
	assertIsObject(data);
	width = data.width;
	height = data.height;
	simulation = createSimulation(width, height, playerData);
	simulation.init();
};

const onCellList = newCells => {
	assertIsArray(newCells);
	for (let i = 0; i < newCells.length; i++) {
		const newCell = newCells[i];
		simulation.setCellState({x: newCell.x, y: newCell.y}, newCell.alive, newCell.ownerId);
	}
	if (firstMapData) {
		advanceSimulation();
		firstMapData = false;
	}
};

const onTickData = data => {
	assertIsObject(data);
	if (simulationSteps === -1) {
		simulationSteps = data.iterations;
	}
	const tick = data.iterations;
	advanceSimulation();
	recentlyClicked = false;
};


const onPlaceShape = () => {
	if (!selectedShape || recentlyClicked) {
		return;
	}

	const shapeCells = selectedShape.shape;

	const indices = [];
	for (let i = 0; i < shapeCells.length; i++) {
		const el = shapeCells[i];
		if (el.bit === "1") {
			const index = getIndexOffsetFromMouse(el.x, el.y);
			indices.push(index);
		}
	}

	recentlyClicked = true;
	notify(SHAPE_PLACED, indices);
};

const onPlayerAdded = newPlayerData => {
	assertIsObject(newPlayerData);
	const {
		playerId,
		color,
		name
	} = newPlayerData;

	playerData[playerId] = {color, name};
	simulation.setPlayerData(playerData);
	notify(PLAYER_DATA_UPDATED, playerData);
};

const onPlayerRemoved = playerId => {
	assertIsNumber(playerId);
	delete playerData[playerId];
	notify(PLAYER_DATA_UPDATED, playerData);
};

const onPlayerPoints = ({playerId, points}) => {
	playerData[playerId].points = points;
	notify(PLAYER_DATA_UPDATED, playerData);
};

export const createGame = sketch => {
	assertIsObject(sketch);

	const game = {};

	game.getWidth = () => {
		return width;
	};

	game.getHeight = () => {
		return height;
	};

	game.getCellSize = () => {
		return cellSize;
	};

	game.getFPS = () => {
		return FPS;
	};

	game.draw = () => {
		sketch.background(245);
		framesRendered++;
		render();
	};

	on(CELL_LIST, onCellList);
	on(PLAYER_IDENTITY, onPlayerIdentity);
	on(MAP_DATA, onMapData);
	on(TICK_DATA, onTickData);
	on(PLAYER_DATA, onPlayerData);
	on(PLAYER_ADDED, onPlayerAdded);
	on(PLAYER_REMOVED, onPlayerRemoved);
	on(PLAYER_POINTS, onPlayerPoints);
	on(SHAPE_SELECTED, onShapeSelected);
	on(PLACE_SHAPE, onPlaceShape);

	simulation.init();

	return game;
};