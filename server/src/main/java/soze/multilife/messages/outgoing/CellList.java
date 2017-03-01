package soze.multilife.messages.outgoing;

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

}
