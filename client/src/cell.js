import {rectRenderFunction} from "./renderer";
import {throwError} from "./utils";

const makeCellCreator = (size, renderFunction) => {
	if(typeof size !== "number") {
		throwError("Size has to be a number, it was: " + size);
	}
	return cell.bind(null, size, renderFunction);
};

const validateCellCreatorInput = (x, y, alive, ownerId, color) => {
	if(typeof x !== "number") {
		throwError("X coordinate has to be a number, it was: " + x);
	}
	if(typeof y !== "number") {
		throwError("Y coordinate has to be a number, it was: " + y);
	}
	if(typeof alive !== "boolean") {
		throwError("alive state has to be a boolean, it was: " + alive);
	}
	if(typeof ownerId !== "number") {
		throwError("OwnerId has to be a number, it was: " + ownerId);
	}
	if(typeof color !== "string") {
		throwError("Color has to be a string, it was: " + color);
	}
};

/**
 * Represents a single cell on a grid.
 */
const cell = (size, renderFunction, x, y, alive, ownerId, color) => {

	validateCellCreatorInput(x, y, alive, ownerId, color);

	let targetSizePercentage = alive ? 1 : 0;
	let currentPercentageSize = 0;

	const cell = {};

	cell.isAlive = () => {
		return alive;
	};

	cell.setAlive = (nextAliveState) => {
		alive = nextAliveState;
		if (alive) {
			targetSizePercentage = 1;
		} else {
			targetSizePercentage = 0;
		}
		return alive;
	};

	cell.getOwnerId = () => {
		return ownerId;
	};

	cell.setOwnerId = newOwnerId => {
		ownerId = newOwnerId;
	};

	cell.getColor = () => {
		return color;
	};

	cell.setColor = newColor => {
		color = newColor;
	};

	cell.update = () => {
		const p = currentPercentageSize;
		const t = targetSizePercentage;
		if (p < t) {
			currentPercentageSize += 0.250;
		} else if (p > t) {
			currentPercentageSize -= 0.250;
		}
	};

	cell.render = viewport => {
		if (currentPercentageSize > 0) {
			if ((x * size) < viewport.x || (x * size) > viewport.x + viewport.width) {
				return;
			}
			if ((y * size) < viewport.y || (y * size) > viewport.y + viewport.height) {
				return;
			}
			cell.actuallyRender();
		}
	};

	cell.actuallyRender = () => {
		const cellSize = size * currentPercentageSize;
		renderFunction(
			(x * size) + (1 - cellSize) * 0.5,
			(y * size) + (1 - cellSize) * 0.5,
			cellSize, cellSize, color
		);
	};

	cell.getPosition = () => {
		return {x, y};
	};

	return cell;
};

export const cellCreator = makeCellCreator(10, rectRenderFunction);