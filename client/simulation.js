/**
 * Created by KJurek on 27.02.2017.
 */

class Simulation {

    constructor(width, height) {
        this.width = width;
        this.height = height;
        this.grid = new Grid(width, height);
        this.rules = new Rules();
        this.grid.addRule(0, this.rules.getRule("BASIC"));
    }

    setCellState(position, alive, ownerId) {
        this.grid.setCellState(position, alive, ownerId);
    }

    update() {
        this.grid.updateCells();
        this.grid.render();
        this.grid.transferCells();
    }

}