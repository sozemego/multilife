package soze.multilife.game;

import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handles outgoing messages from players to clients.
 */
public class GameOutgoingMessageHandler implements Game {

	private final Game game;

	public GameOutgoingMessageHandler(Game game) {
		this.game = game;
	}

	public void run() {
		sendClickedData();
		game.run();
		sendIterationsData();
		sendRemainingTime();
		sendPlayerData();
	}

	/**
	 * Sends data about simulation steps ({@link IterationData})
	 * to all players.
	 */
	private void sendIterationsData() {
		IterationData iterationData = new IterationData(game.getIterations());
		sendMessage(iterationData);
	}

	private void sendRemainingTime() {
		TimeRemainingMessage timeRemainingMessage = new TimeRemainingMessage(game.getRemainingTime());
		sendMessage(timeRemainingMessage);
	}

	/**
	 * Sends data about which cells were clicked this iteration.
	 */
	private void sendClickedData() {
		CellList cellList = constructCellList(game.getClickedCells());
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
		PlayerData data = game.getPlayerData();
		sendMessage(data);
	}

	//
	//
	// DELEGATED METHODS
	//
	//

	public int getId() {
		return game.getId();
	}

	public int getMaxPlayers() {
		return game.getMaxPlayers();
	}

	public void acceptMessage(IncomingMessage message, long id) {
		game.acceptMessage(message, id);
	}

	public int getWidth() {
		return game.getWidth();
	}

	public int getHeight() {
		return game.getHeight();
	}

	public Collection<Cell> getAllCells() {
		return game.getAllCells();
	}

	public boolean isOutOfTime() {
		return game.isOutOfTime();
	}

	public boolean isFull() {
		return game.isFull();
	}

	public void end() {
		game.end();
	}

	public long getTickRate() {
		return game.getTickRate();
	}

	public PlayerData getPlayerData() {
		return game.getPlayerData();
	}

	public Collection<Player> getPlayers() {
		return game.getPlayers();
	}

	public int getIterations() {
		return game.getIterations();
	}

	public long getRemainingTime() {
		return game.getRemainingTime();
	}

	public Collection<Cell> getClickedCells() {
		return game.getClickedCells();
	}

	public void addPlayer(Player player) {
		game.addPlayer(player);
	}

	public void removePlayer(long id) {
		game.removePlayer(id);
	}

	public void sendMessage(OutgoingMessage message) {
		game.sendMessage(message);
	}

	public boolean isScheduledForRemoval() {
		return game.isScheduledForRemoval();
	}

	//
	//
	// END DELEGATED METHODS
	//
	//
}
