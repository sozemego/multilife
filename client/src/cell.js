import {rectRenderFunction} from './renderer';
import {throwError} from './utils';
import {assertIsBoolean, assertIsFunction, assertIsNumber, assertIsString} from './assert';

const makeCellCreator = (size, renderFunction) => {
	assertIsNumber(size);
	assertIsFunction(renderFunction);
	return cell.bind(null, size, renderFunction);
};

const validateCellCreatorInput = (x, y, alive, ownerId, color) => {
	assertIsNumber(x);
	assertIsNumber(y);
	assertIsBoolean(alive);
	assertIsNumber(ownerId);
	assertIsString(color);
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