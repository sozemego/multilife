import p5 from "p5";
import {simulation} from "./simulation";
import {cellCreator as createCell} from "./cell";
import {rectRenderFunction as renderFunction} from "./renderer";
import {createGameUI} from "./game-ui";
import {createLoginUi} from "./login-ui";
import {createNetworkLayer} from "./network";
import {
	CELL_LIST, MAP_DATA, PLACE_SHAPE, PLAYER_DATA_UPDATED, PLAYER_IDENTITY,
	PLAYER_REMOVED, SHAPE_PLACED, PLAYER_ADDED, PLAYER_POINTS,
	SHAPE_SELECTED, TICK_DATA, PLAYER_DATA
} from "./events";
import {notify, on, setLogging} from "./event-bus";

/**
 * Separate game class into the following functionalities
 * 1. app
 * 2. network layer. connects, transforms messages into usable objects and calls game methods
 * 3. ui. sends events to game
 * 4. game itself. just runs the game and renders
 */

class Game {

	constructor(sketch) {
		this.sketch = sketch;
		this.framesRendered = 0;
		this.FPS = 60;
		this.stepsPerSecond = 4;
		this.stepPerFrames = this.FPS / this.stepsPerSecond;
		this.simulationSteps = -1;
		this.cellSize = 10;
		this.simulation = simulation(0, 0, {});
		this.simulation.init();
		this.width = 0;
		this.height = 0;
		this.myId = 0;
		this.recentlyClicked = false;
		this.playerData = {};
		this.offsets = {};
		this._tickDuration = 250;
		on(CELL_LIST, this._onCellList);
		on(PLAYER_IDENTITY, this._onPlayerIdentity);
		on(MAP_DATA, this._onMapData);
		on(TICK_DATA, this._onTickData);
		on(PLAYER_DATA, this._onPlayerData);
		on(PLAYER_ADDED, this._onPlayerAdded);
		on(PLAYER_REMOVED, this._onPlayerRemoved);
		on(PLAYER_POINTS, this._onPlayerPoints);
		on(SHAPE_SELECTED, this._onShapeSelected);
		on(PLACE_SHAPE, this._onPlaceShape);
	}

	/**
	 * Returns index of a cell that is (xOffset, yOffset) away from mouse cursor.
	 * xOffset and yOffset should be in pixels.
	 * @param xPixels
	 * @param yPixels
	 * @private
	 */
	_getIndexOffsetFromMouse = (xPixels, yPixels) => {
		return this._getIndex(
			this.sketch.mouseX + (xPixels * this.cellSize),
			this.sketch.mouseY + (yPixels * this.cellSize)
		);
	};

	/**
	 * Returns a index in the cell array of a point at location x, y.
	 * x, y represents a point on a canvas (in pixels).
	 * @param x
	 * @param y
	 * @returns {number}
	 * @private
	 */
	_getIndex = (x, y) => {
		x = x / this.cellSize;
		y = y / this.cellSize;
		const index = Math.floor(x) + (Math.floor(y) * this.width);
		const maxSize = this.width * this.height;
		if (index < 0) return index + (maxSize); // wrap around if neccesary
		if (index >= maxSize) return index % (maxSize);
		return index; // return index if not neccesary to wrap
	};

	draw = () => {
		this.sketch.background(245);
		this.framesRendered++;
		this._render();
	};

	_advanceSimulation = () => {
		this.simulation.update();
		this.simulationSteps++;
	};

	/**
	 Renders cells.
	 */
	_render = () => {
		this._renderSelectedShape();
		this.simulation.render(this._getViewport());
	};

	_renderSelectedShape = () => {
		if (!this.selectedShape) {
			return;
		}

		const shape = this.selectedShape.shape;
		const indices = this._findIndices(shape.filter((p) => p.bit === "1").map((p) => {
			return {x: p.x, y: p.y}
		}));

		const positions = this._findPositions(indices);
		for (let i = 0; i < positions.length; i++) {
			const p = positions[i];
			renderFunction(
				p.x + (1 - this.cellSize) * 0.5, p.y + (1 - this.cellSize) * 0.5,
				this.cellSize, this.cellSize, "#adeedd"
			);
		}
	};

	_getMousePosition = () => {
		return {
			x: this.sketch.mouseX,
			y: this.sketch.mouseY
		};
	};

	/**
	 * Finds x,y positions of a given array of indices.
	 * The indices are indices of a 1D array which represents
	 * cells.
	 * Returned positions are multiplied by cellSize, so they represent
	 * coordinates were cell should be drawn.
	 * @param indices
	 * @returns {Number|*|Array}
	 * @private
	 */
	_findPositions = (indices) => {
		return indices.map((i) => {
			let x = i % this.width;
			let y = Math.floor(i / this.width);
			return {x: x * this.cellSize, y: y * this.cellSize};
		});
	};

	/**
	 * Finds indices in 1D array of x, y positions.
	 * The positions are translated by current mouse coorinates,
	 * so they are positions in relation to the mouse cursor.
	 * @param positions
	 * @returns {Array}
	 * @private
	 */
	_findIndices = (positions) => {
		const indices = [];
		for (let i = 0; i < positions.length; i++) {
			indices.push(this._getIndexOffsetFromMouse(positions[i].x, positions[i].y));
		}
		return indices;
	};

	_getViewport = () => {
		return {
			x: window.scrollX,
			y: window.scrollY,
			width: window.innerWidth,
			height: window.innerHeight
		};
	};

	_onShapeSelected = shape => {
		this.selectedShape = shape;
	};

	_onPlayerIdentity = ({playerId}) => {
		this.myId = playerId;
	};

	_onPlayerData = (msg) => {
		this.playerData = msg;
		this.simulation.setPlayerData(msg);
		this._renderPlayerPoints();
	};

	_onMapData = (data) => {
		this.width = data.width;
		this.height = data.height;
		this.simulation = simulation(this.width, this.height, this.playerData);
		this.simulation.init();
	};

	_onCellList = newCells => {
		for (let i = 0; i < newCells.length; i++) {
			const newCell = newCells[i];
			this.simulation.setCellState({x: newCell.x, y: newCell.y}, newCell.alive, newCell.ownerId);
		}
		if (this.firstMapData) {
			this._advanceSimulation();
			this.firstMapData = false;
		}
	};

	_onTickData = (data) => {
		if (this.simulationSteps === -1) {
			this.simulationSteps = data.iterations;
		}
		const tick = data.iterations;
		const difference = tick - this.simulationSteps;
		this._advanceSimulation();
		this.recentlyClicked = false;
	};

	getFPS = () => {
		return this.FPS;
	};

	_onPlaceShape = () => {
		const {selectedShape: shape} = this;
		if(!shape || this.recentlyClicked) {
			return;
		}

		const shapeCells = shape.shape;

		const indices = [];
		for (let i = 0; i < shapeCells.length; i++) {
			const el = shapeCells[i];
			if (el.bit === "1") {
				const index = this._getIndexOffsetFromMouse(el.x, el.y);
				indices.push(index);
			}
		}

		this.recentlyClicked = true;
		notify(SHAPE_PLACED, indices);
	};

	_onPlayerAdded = newPlayerData => {
		const {
			playerId,
			color,
			name
		} = newPlayerData;

		this.playerData[playerId] = {color, name};
		this.simulation.setPlayerData(this.playerData);
		notify(PLAYER_DATA_UPDATED, this.playerData);
	};

	_onPlayerRemoved = playerId => {
		delete this.playerData[playerId];
		notify(PLAYER_DATA_UPDATED, this.playerData);
	};

	_onPlayerPoints = ({playerId, points}) => {
		this.playerData[playerId].points = points;
		notify(PLAYER_DATA_UPDATED, this.playerData);
	};

}

export const sketch = new p5(p => {

	let loginUi;
	let gameUi;
	let game;

	p.setup = () => {
		setLogging(false);
		createNetworkLayer(WEBSOCKET_HOST);
		loginUi = createLoginUi();
		loginUi.createLoginView();
		gameUi = createGameUI(p.createCanvas(window.innerWidth, window.innerHeight));
		game = new Game(p);
		p.frameRate(game.getFPS());
	};

	p.keyPressed = (event) => {
		gameUi.keyPressed(event.keyCode);
	};

	p.draw = () => {
		game.draw();
	};

	p.mouseReleased = () => {
		gameUi.onMouseUp();
	};

	p.windowResized = () => {
		p.resizeCanvas(
			Math.max(window.innerWidth, game.width * game.cellSize),
			Math.max(window.innerHeight, game.height * game.cellSize)
		);
	};
});