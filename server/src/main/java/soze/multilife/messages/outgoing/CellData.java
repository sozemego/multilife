package soze.multilife.messages.outgoing;

import soze.multilife.game.Cell;

/**
 * Data about one cell.
 */
public class CellData {

	public final int x;
	public final int y;
	public final boolean alive;
	public final int ownerId;

	public CellData(Cell cell) {
		this.x = cell.getX();
		this.y = cell.getY();
		this.alive = cell.isAlive();
		this.ownerId = cell.getOwnerId();
	}

}
