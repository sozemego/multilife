package soze.multilife.messages.outgoing;

import soze.multilife.simulation.Cell;

/**
 * Created by soze on 2/21/2017.
 */
public class CellData {

  public final int x;
  public final int y;
  public final boolean alive;
  public final long ownerId;

  public CellData(Cell cell) {
	this.x = cell.getX();
	this.y = cell.getY();
	this.alive = cell.isAlive();
	this.ownerId = cell.getOwnerId();
  }

}
