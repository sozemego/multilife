package soze.multilife.messages.outgoing;

import java.util.List;

/**
 * Created by soze on 2/21/2017.
 */
public class CellList extends OutgoingMessage {

	public final List<CellData> cells;

	public CellList(List<CellData> cells) {
		this.cells = cells;
		this.setType(Type.CELL_LIST);
	}

}
