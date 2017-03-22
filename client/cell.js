class Cell {

	constructor(x, y, alive = false, ownerId = 0, size = 25, color, renderFunction) {
		this.x = x;
		this.y = y;
		this.alive = this.setAlive(alive);
		this.ownerId = ownerId;
		this.size = size;
		this.color = color;
		this.renderFunction = renderFunction;

		this.targetSizePercentage = alive ? 1 : 0;
		this.currentPercentageSize = 0;
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
		let p = this.currentPercentageSize;
		let t = this.targetSizePercentage;
		if(p < t) {
			this.currentPercentageSize += 0.250;
		} else if (p > t) {
			this.currentPercentageSize -= 0.250;
		} else {
			this.active = false;
		}
	}

	render(viewport) {
		if(this.alive) {
		    if((this.x * this.size) < viewport.x || (this.x * this.size) > viewport.x + viewport.width) {
		        return;
		    }
		    if((this.y * this.size) < viewport.y || (this.y * this.size) > viewport.y + viewport.height) {
		        return;
		    }
			this.actuallyRender();
		}
	}

	actuallyRender() {
		fill(this.color);
		this.currentPercentageSize = 1;
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
