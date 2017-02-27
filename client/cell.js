class Cell {

	constructor(x, y, alive = false, ownerId = 0, size = 25, color, renderFunction) {
		this.x = x;
		this.y = y;
		this.alive = alive;
		this.ownerId = ownerId;
		this.size = size;
		this.color = color;
		this.renderFunction = renderFunction;

		this.targetSizePercentage = 1;
		this.currentPercentageSize = 1;
		this.active = false;
	}

	isAlive() {
		return this.alive;
	}

	setAlive(alive) {
		if(this.alive !== alive) {
			this.active = true;
		}
		this.alive = alive;
		if(alive) {
			this.targetSizePercentage = 1;
		} else {
			this.targetSizePercentage = 0;
		}
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
		let p = this.currentPercentageSize;
		let t = this.targetSizePercentage;
		if(p < t) {
			this.currentPercentageSize += 0.125;
		} else if (p > t) {
			this.currentPercentageSize -= 0.125;
		} else {
			this.active = false;
		}
	}

	render() {
		if(this.alive || this.active) {
			this.actuallyRender();
            textSize(16);
            fill(125);
            text(this.x + "," + this.y, this.x * this.cellSize, this.y * this.cellSize);
		}
	}

	actuallyRender() {
		fill(this.color);
		let cellSize = this.size * this.currentPercentageSize;
		this.renderFunction(
		(this.x * this.size) + (1 - cellSize) * 0.5,
		(this.y * this.size) + (1 - cellSize) * 0.5,
		cellSize, cellSize);

	}

	static get rectRenderFunction() {
		return function(x, y, width, height) {
			rect(x, y, width, height);
		};
	}

	static get ellipseRenderFunction() {
		return function(x, y, width, height) {
			ellipse(x + width / 2, y + height / 2, width, height);
		};
	}

}
