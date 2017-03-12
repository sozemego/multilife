class Grid {

    constructor(width, height, playerData) {
        this.width = width;
        this.height = height;
        this.cells = {};
        this.activeCells = {};
        this.nextCells = {};
        this.playerData = playerData;
        this.cellSize = 10;
        this.rules = new Rules();
        this.init();
    }

    /**
     * Creates all cells.
     */
    init() {
        for(let i = 0; i < width; i++) {
            for(let j = 0; j < height; j++) {
                this.cells["x:" + i + "y:" + j] = new Cell(i, j, false, 0, this.cellSize, this.getColor(0), Cell.ellipseRenderFunction);
            }
        }
    }

    /**
        Sets the state of a cell at a given position.
        The cell is in nextCells object.
    */
    setCellState(position, alive, ownerId) {
        let cell = this.nextCells[this.getPositionKey(position)];
        if(!cell) {
            cell = new Cell(position.x, position.y, alive, ownerId, this.cellSize, this.getColor(ownerId), Cell.ellipseRenderFunction);
            this.nextCells[this.getPositionKey(position)] = cell;
        }
    }

    /**
        Transforms the position of a cell into the key used in maps
        containing the cells.
    */
    getPositionKey(position) {
        let x = position.x;
        let y = position.y;
        if(x < 0) {
            x = this.width - 1;
        }
        if(x === this.width) {
            x = 0;
        }
        if(y < 0) {
            y = this.height - 1;
        }
        if(y === this.height) {
            y = 0;
        }
        return "x:" + x + "y:" + y;
    }

    getColor(ownerId) {
        return this.playerData.colors[ownerId] || "#000000";
    }

    updateCells() {
        for(let pos in this.activeCells) {
            if (this.activeCells.hasOwnProperty(pos)) {
                let cell = this.activeCells[pos];
                let x = cell.x;
                let y = cell.y;
                let aliveNeighbours = this.getAliveNeighbourCells(x, y);
                let ownerId = cell.getOwnerId();
                let state = this.rules.getRule(this.playerData.rules[ownerId])(aliveNeighbours.length, cell.isAlive());
                if (state != 0) {
                    let strongestOwnerId = this.getStrongestOwnerId(aliveNeighbours);
                    this.setCellState({x:x, y:y}, state > 0, strongestOwnerId == -1 ? cell.getOwnerId() : strongestOwnerId);
                }
            }
        }
    }

    /**
        Returns an array of alive cells neighbouring a cell at x, y.
    */
    getAliveNeighbourCells(x, y) {
       let cells = [];
       for(let i = -1; i < 2; i++) {
           for(let j = -1; j < 2; j++) {
               if(i === 0 && j === 0) continue;
               let positionKey = this.getPositionKey({x: i + x, y: j + y});
               let cell = this.cells[positionKey];
               if(cell.isAlive()) cells.push(cell);
           }
       }
       return cells;
    }

    getStrongestOwnerId(cells) {
        if(cells.length === 0) {
            return -1;
        }

        let ownerIds = cells.map(function(cell, index) {
           return cell.getOwnerId();
        });

        return this.mode(ownerIds);
    }

    mode(ownerIds) {
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
    }

    transferCells() {
        this.activeCells = {};
        for(let pos in this.nextCells) {
            if(this.nextCells.hasOwnProperty(pos)) {
                let oldCell = this.nextCells[pos];
                let c = this.cells[pos];
                c.setAlive(oldCell.isAlive());
                c.setOwnerId(oldCell.getOwnerId());
                c.setColor(this.getColor(oldCell.getOwnerId()));
                this.addToActive(c);
            }
        }
        this.nextCells = {};
    }

    addToActive(cell) {
        let x = cell.x;
        let y = cell.y;
        for(let i = -1; i < 2; i++) {
            for(let j = -1; j < 2; j++) {
                let position = this.getPositionKey({x: i + x, y: j + y});
                let cell = this.activeCells[position];
                if(!cell) {
                    this.activeCells[position] = this.cells[position];
                }
            }
        }

    }

    render(viewport) {
        for(let pos in this.cells) {
            if(this.cells.hasOwnProperty(pos)) {
				this.cells[pos].update();
                this.cells[pos].render(viewport);
            }
        }
    }

    setPlayerData(playerData) {
        this.removeDisconnectedPlayers(playerData);
        Object.assign(this.playerData, playerData);
    }

    removeDisconnectedPlayers(newPlayerData) {
        let oldData = this.playerData;
        let oldPlayerIds = this.getPlayerIds(oldData.rules);
        let newPlayerIds = this.getPlayerIds(newPlayerData.rules);
        let disconnectedPlayers = this.findDisconnectedPlayers(oldPlayerIds, newPlayerIds);
        for(let i = 0; i < disconnectedPlayers.length; i++) {
            let id = disconnectedPlayers[i];
            for(let pos in this.cells) {
                if(this.cells.hasOwnProperty(pos)) {
                    let cell = this.cells[pos];
                    if(cell.ownerId == id) {
                        cell.setAlive(false);
                        cell.setOwnerId(0);
                    }
                }
            }
        }
    }

    getPlayerIds(playerData) {
        let arr = [];
        for(let id in playerData) {
            if(playerData.hasOwnProperty(id)) {
                arr.push(id);
            }
        }
        return arr;
    }

    findDisconnectedPlayers(oldPlayerIds, newPlayerIds) {
        let disconnectedPlayers = [];
        for(let i = 0; i < oldPlayerIds.length; i++) {
            let oldPlayerId = oldPlayerIds[i];
            let index = newPlayerIds.findIndex((i) => i === oldPlayerId);
            if(index === -1) {
                disconnectedPlayers.push(oldPlayerId);
            }
        }
        return disconnectedPlayers;
    }

}
