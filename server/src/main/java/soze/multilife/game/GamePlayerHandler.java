package soze.multilife.game;

import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A decorator layer which handles players joining the game.
 */
public class GamePlayerHandler implements Game {

	private final Game game;

	/**
	 * Players which recently joined, have not received map data yet.
	 */
	private final List<Player> freshPlayers = new ArrayList<>();

	/**
	 * Ids of players which disconnected, but were not processed yet.
	 */
	private final List<Long> leavingPlayerIds = new ArrayList<>();

	public GamePlayerHandler(Game game) {
		this.game = game;
	}

	public void run() {
		updateLeavingPlayers();
		updateFreshPlayers();
		game.run();
	}

	public void addPlayer(Player player) {
		synchronized (freshPlayers) {
			freshPlayers.add(player);
		}
	}

	public void removePlayer(long id) {
		synchronized (leavingPlayerIds) {
			leavingPlayerIds.add(id);
		}
	}

	private void updateLeavingPlayers() {
		synchronized (leavingPlayerIds) {
			for (long playerId : leavingPlayerIds) {
				game.removePlayer(playerId);
			}
			leavingPlayerIds.clear();
		}
	}

	private void updateFreshPlayers() {
		synchronized (freshPlayers) {
			if (!freshPlayers.isEmpty()) {

				freshPlayers.forEach(game::addPlayer);
				freshPlayers.clear();

				sendPlayerData();
				sendMapData();
				sendAllAliveCellData();

			}
		}
	}

	/**
	 * Sends map data (width, height) to all players.
	 */
	private void sendMapData() {
		MapData mapData = getMapData();
		sendMessage(mapData);
	}

	/**
	 * Assembles and returns MapData.
	 *
	 * @return MapData
	 */
	private MapData getMapData() {
		return new MapData(game.getWidth(), game.getHeight());
	}

	/**
	 * Sends data about all players in this instance.
	 */
	private void sendPlayerData() {
		PlayerData data = game.getPlayerData();
		sendMessage(data);
	}

	/**
	 * Sends data about all alive cells to all players.
	 */
	private void sendAllAliveCellData() {
		CellList list = getAllAliveCellData();
		sendMessage(list);
	}

	/**
	 * Assembles {@link CellList} from all alive cells.
	 *
	 * @return CellList
	 */
	private CellList getAllAliveCellData() {
		List<Cell> cells = game.getAllCells().stream()
			.filter(Cell::isAlive)
			.collect(Collectors.toList());
		return constructCellList(cells);
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

	//
	//
	// DELEGATED METHODS
	//
	//
	public int getId() {
		return game.getId();
	}

	public void acceptMessage(IncomingMessage message, long playerId) {
		game.acceptMessage(message, playerId);
	}

	public void end() {
		game.end();
	}

	public long getTickRate() {
		return game.getTickRate();
	}

	public int getMaxPlayers() {
		return game.getMaxPlayers();
	}

	public PlayerData getPlayerData() {
		return game.getPlayerData();
	}

	public Collection<Player> getPlayers() {
		return game.getPlayers();
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

	public int getIterations() {
		return game.getIterations();
	}

	public long getRemainingTime() {
		return game.getRemainingTime();
	}

	public Collection<Cell> getClickedCells() {
		return game.getClickedCells();
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
