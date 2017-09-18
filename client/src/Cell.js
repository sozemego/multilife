/**
 * Represents a single cell on a grid.
 */
export default class Cell {

	constructor(x, y, alive, ownerId, size, color, renderFunction) {
		this.x = x;
		this.y = y;
		this.alive = this.setAlive(alive);
		this.ownerId = ownerId;
		this.size = size || 10;
		this.color = color || "#000000";
		this.renderFunction = renderFunction;

		this.targetSizePercentage = alive ? 1 : 0;
		this.currentPercentageSize = 0;
	}

	isAlive() {
		return this.alive;
	}

	setAlive(alive) {
		this.alive = alive;
		if (alive) {
			this.targetSizePercentage = 1;
		} else {
			this.targetSizePercentage = 0;
		}
		return alive;
	}

	getOwnerId() {
		return this.ownerId;
	}

	setOwnerId(id) {
		this.ownerId = id;
	}

	getColor() {
		return this.color;
	}

	setColor(color) {
		this.color = color;
	}

	update() {
		const p = this.currentPercentageSize;
		const t = this.targetSizePercentage;
		if (p < t) {
			this.currentPercentageSize += 0.250;
		} else if (p > t) {
			this.currentPercentageSize -= 0.250;
		} else {
			this.active = false;
		}
	}

	render(viewport) {
		if (this.currentPercentageSize > 0) {
			if ((this.x * this.size) < viewport.x || (this.x * this.size) > viewport.x + viewport.width) {
				return;
			}
			if ((this.y * this.size) < viewport.y || (this.y * this.size) > viewport.y + viewport.height) {
				return;
			}
			this.actuallyRender();
		}
	}

	actuallyRender() {
		const cellSize = this.size * this.currentPercentageSize;
		this.renderFunction(
			(this.x * this.size) + (1 - cellSize) * 0.5,
			(this.y * this.size) + (1 - cellSize) * 0.5,
			cellSize, cellSize, this.getColor()
		);

	}

}
