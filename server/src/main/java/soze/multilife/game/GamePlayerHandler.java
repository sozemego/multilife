package soze.multilife.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.exceptions.PlayerAlreadyInGameException;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.outgoing.CellData;
import soze.multilife.messages.outgoing.CellList;
import soze.multilife.messages.outgoing.MapData;
import soze.multilife.messages.outgoing.PlayerData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A decorator layer which handles players joining the game.
 */
public class GamePlayerHandler extends GameDecorator {

	private static final Logger LOG = LoggerFactory.getLogger(GamePlayerHandler.class);

	/**
	 * Players which recently joined, have not received map data yet.
	 */
	private final List<Player> freshPlayers = new ArrayList<>();

	/**
	 * Ids of players which disconnected, but were not processed yet.
	 */
	private final List<Long> leavingPlayerIds = new ArrayList<>();

	GamePlayerHandler(Game game) {
		super(game);
	}

	public void run() {
		updateLeavingPlayers();
		updateFreshPlayers();
		super.run();
	}

	public boolean addPlayer(Player player) throws PlayerAlreadyInGameException {
		synchronized (freshPlayers) {
			if(getPlayers().size() + freshPlayers.size() == getMaxPlayers()) {
				return false;
			}
			freshPlayers.add(player);
		}
		return true;
	}

	public void removePlayer(long id) {
		synchronized (leavingPlayerIds) {
			leavingPlayerIds.add(id);
		}
	}

	private void updateLeavingPlayers() {
		synchronized (leavingPlayerIds) {
			for (long playerId : leavingPlayerIds) {
				try {
					super.removePlayer(playerId);
				} catch (PlayerNotInGameException e) {
					LOG.warn("Trying to remove a player which does not exist.", e);
				}
			}
			leavingPlayerIds.clear();
		}
	}

	private void updateFreshPlayers() {
		synchronized (freshPlayers) {
			if (!freshPlayers.isEmpty()) {

				for(Player player: freshPlayers) {
					try {
						super.addPlayer(player);
					} catch (PlayerAlreadyInGameException e) {

					}
				}

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
		return new MapData(super.getWidth(), super.getHeight());
	}

	/**
	 * Sends data about all players in this instance.
	 */
	private void sendPlayerData() {
		PlayerData data = super.getPlayerData();
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
		List<Cell> cells = super.getAllCells().values().stream()
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

}
