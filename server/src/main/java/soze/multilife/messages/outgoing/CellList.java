package soze.multilife.messages.outgoing;

import soze.multilife.game.Cell;

import java.util.List;

/**
 * Data about a list of cells.
 */
public class CellList extends OutgoingMessage {

  public final List<CellData> cells;

  public CellList(List<CellData> cells) {
    this.setType(OutgoingType.CELL_LIST);
    this.cells = cells;
  }

  public void accept(OutgoingMessageVisitor visitor) {
    visitor.visit(this);
  }

  /**
   * Data about one cell.
   */
  public static class CellData {

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

}
