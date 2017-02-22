package soze.multilife.messages.outgoing;

import java.util.List;

/**
 * Created by soze on 2/21/2017.
 */
public class CellList extends OutgoingMessage {

  public final List<CellData> cells;

  public CellList(List<CellData> cells) {
    this.setType(OutgoingType.CELL_LIST);
    this.cells = cells;
  }

}
