
class Simulation {

    constructor(width, height, playerData) {
        this.width = width;
        this.height = height;
        this.grid = new Grid(width, height, playerData);
    }

    setCellState(position, alive, ownerId) {
        this.grid.setCellState(position, alive, ownerId);
    }

    setPlayerData(playerData) {
        this.grid.setPlayerData(playerData);
    }

	render() {
		this.grid.render();
	}

    update() {
        this.grid.updateCells();
    }

	transferCells() {
		this.grid.transferCells();
	}

}
