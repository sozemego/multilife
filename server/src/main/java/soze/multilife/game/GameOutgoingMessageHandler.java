package soze.multilife.game;

import soze.multilife.messages.outgoing.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handles outgoing messages from game/players to clients.
 */
public class GameOutgoingMessageHandler extends GameDecorator {

	GameOutgoingMessageHandler(Game game) {
		super(game);
	}

	public void run() {
		sendClickedData();
		super.run();
		sendIterationsData();
		sendRemainingTime();
		sendPlayerData();
	}

	/**
	 * Sends data about simulation steps ({@link IterationData})
	 * to all players.
	 */
	private void sendIterationsData() {
		IterationData iterationData = new IterationData(super.getIterations());
		sendMessage(iterationData);
	}

	private void sendRemainingTime() {
		TimeRemainingMessage timeRemainingMessage = new TimeRemainingMessage(super.getRemainingTime());
		sendMessage(timeRemainingMessage);
	}

	/**
	 * Sends data about which cells were clicked this iteration.
	 */
	private void sendClickedData() {
		CellList cellList = constructCellList(super.getClickedCells());
		sendMessage(cellList);
	}

	/**
	 * Assembles {@link CellList} from a given list of cells.
	 *
	 * @param cells cells
	 * @return CellList
	 */
	private CellList constructCellList(Collection<Cell> cells) {
		List<CellData> cellData = new ArrayList<>();
		for (Cell cell : cells) {
			cellData.add(new CellData(cell));
		}
		return new CellList(cellData);
	}

	/**
	 * Sends data about all players in this instance.
	 */
	private void sendPlayerData() {
		PlayerData data = super.getPlayerData();
		sendMessage(data);
	}

}
