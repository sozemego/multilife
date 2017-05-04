package soze.multilife.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.rule.RuleFactory;
import soze.multilife.game.rule.RuleType;
import soze.multilife.messages.incoming.ClickMessage;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.outgoing.*;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * An object which handles the simulation ({@link Grid}) and players.
 * This object is responsible for advancing the simulation and sending
 * data to players.
 */
public class Game {

	private static final Logger LOG = LoggerFactory.getLogger(Game.class);

	/**
	 * playerId of this simulation.
	 */
	private static final long SIMULATION_PLAYER_ID = 0L;

	private final long id;

	private boolean scheduledForRemoval;

	private final long tickRate;

	/**
	 * Players which recently joined, have not received map data yet.
	 */
	private final List<Player> freshPlayers = new ArrayList<>();

	/**
	 * Ids of players which disconnected, but were not processed yet.
	 */
	private final List<Long> leavingPlayerIds = new ArrayList<>();

	/**
	 * Players already in-game.
	 */
	private final Map<Long, Player> players = new HashMap<>();

	private final int maxPlayers;

	/**
	 * Maps playerId to color (#hex) of their alive cells.
	 */
	private final Map<Long, String> playerColors = new HashMap<>();

	/**
	 * Colors which are available for players.
	 */
	private final String[] availableColors = new String[]{"#ff0000", "#00ff00", "#0000ff", "#ffff00"};
	/**
	 * Width and height of the game grid.
	 */
	private final int width;

	private final int height;

	/**
	 * Time for this instance to live in ms.
	 */
	private final long duration;
	private long timePassed;
	private long t0 = -1;


	/**
	 * Objects managing the cells. It manages all cells as well as the active cells and the cells
	 * that will be active in the next generation.
	 */
	private final Grid grid;

	/**
	 * Cells clicked by players this iteration.
	 */
	private final Set<Cell> clickedCells = new HashSet<>();
	/**
	 * A set of playerIds who clicked cells this iteration.
	 * For limiting purposes.
	 */
	private final Set<Long> clickedPlayers = new HashSet<>();

	/**
	 * Percent of alive cells spawned on simulation start.
	 */
	private final float initialDensity = 0.5f;

	/**
	 * A count for how many times this simulation advanced.
	 */
	private int simulationSteps = 0;

	Game(long id, int width, int height, int maxPlayers, long duration, long tickRate) {
		this.id = id;
		this.width = width;
		this.height = height;
		this.grid = new Grid(width, height);
		grid.addRule(SIMULATION_PLAYER_ID, RuleFactory.getRule(RuleType.BASIC));
		this.maxPlayers = maxPlayers;
		this.duration = duration;
		this.tickRate = tickRate;
	}

	/**
	 * Spawns initial living cells.
	 */
	public void init() {
		SecureRandom random = new SecureRandom();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (random.nextFloat() < initialDensity) {
					grid.changeState(i, j, true, SIMULATION_PLAYER_ID);
				}
			}
		}
		grid.updateGrid(); // runs the simulation once, so that the first player logging can receive some data
	}

	public long getId() {
		return id;
	}

	/**
	 * Adds a player to the simulation.
	 *
	 * @param player player
	 */
	public void addPlayer(Player player) {
		synchronized (freshPlayers) {
			freshPlayers.add(player);
		}
	}

	/**
	 * Removes a player from the simulation.
	 *
	 * @param id id of the player to remove
	 */
	public void removePlayer(long id) {
		synchronized (leavingPlayerIds) {
			leavingPlayerIds.add(id);
		}
	}

	/**
	 * When a player sends an array of indices of cells they clicked,
	 * this method checks if all of them are currently not alive.
	 * If so, marks them as alive, sets their owner as the player who clicked
	 * and sends information to all players about newly alive cells.
	 * If a player already clicked cells during this iteration, this method returns.
	 *
	 * @param indices indices of cells to click
	 * @param id      id of the player
	 */
	public void click(int[] indices, long id) {
		if (clickedPlayers.contains(id)) { //only one click per iteration
			return;
		}
		LOG.trace("Player [{}] wants to click on [{}] cells.", id, indices.length);
		List<Cell> clickableCells = grid.findClickableCells(indices, id);
		if (clickableCells.size() == indices.length) {
			for (Cell cell : clickableCells) {
				if (clickedCells.contains(cell)) {
					return;
				}
			}
			clickedPlayers.add(id);
			clickedCells.addAll(clickableCells);
		}
	}

	/**
	 * Updates the simulation. It sends data for newly logged in players,
	 * advances the simulation by one iteration, sends data to players
	 * and switches grids.
	 */
	public void update() {
		handleMessages();
		updateLeavingPlayers();
		updateFreshPlayers();
		updateTime();
		if (!players.isEmpty()) {
			grid.click(clickedCells);
			sendClickedCells();
			grid.updateGrid();
			simulationSteps++;
			sendSimulationSteps();
			sendPlayerData();
			sendRemainingTime();
		}
		if(isOutOfTime()) {
			setScheduledForRemoval(true);
		}
	}

	/**
	 * Processes players who recently joined.
	 * Assigns them a color and loads their game of life rule.
	 * It then adds them to the players collection, and updates all players
	 * with map data. Also notifies all players of all player data.
	 */
	private void updateFreshPlayers() {
		synchronized (freshPlayers) {
			if (!freshPlayers.isEmpty()) {

				for (Player p : freshPlayers) {
					playerColors.put(p.getId(), availableColors[players.size() % availableColors.length]);
					grid.addRule(p.getId(), RuleFactory.getRule(p.getRule()));
				}

				synchronized (players) {
					Iterator<Player> it = freshPlayers.iterator();
					while (it.hasNext()) {
						Player player = it.next();
						players.put(player.getId(), player);
						it.remove();
					}

					sendPlayerData();
					sendMapData();
					sendAllAliveCellData();
					synchronize();
				}
			}
		}
	}

	private void updateLeavingPlayers() {
		synchronized (leavingPlayerIds) {
			for (long playerId : leavingPlayerIds) {
				playerColors.put(playerId, "#000000");
				players.remove(playerId);
				grid.killAll(playerId);
			}
			leavingPlayerIds.clear();
		}
	}

	/**
	 * Sends data about all players in this instance.
	 */
	private void sendPlayerData() {
		PlayerData data = createPlayerData();
		sendToPlayers(data);
	}

	/**
	 * Creates {@link PlayerData} object.
	 * This object contains information about player points, names, colors and rules.
	 *
	 * @return PlayerData
	 */
	private PlayerData createPlayerData() {
		Map<Long, Long> points = grid.getPlayerPoints();
		Map<Long, String> names = new HashMap<>();
		Map<Long, String> colors = new HashMap<>();
		Map<Long, String> rules = new HashMap<>();

		// data for the simulation
		points.put(0L, 0L);
		names.put(0L, "AI");
		colors.put(0L, "#000000");
		rules.put(0L, "BASIC");

		for (Player player : players.values()) {
			names.put(player.getId(), player.getName());
			colors.put(player.getId(), playerColors.get(player.getId()));
			rules.put(player.getId(), player.getRule());
		}
		return new PlayerData(points, names, colors, rules);
	}

	/**
	 * Assembles and returns MapData.
	 *
	 * @return MapData
	 */
	private MapData getMapData() {
		return new MapData(width, height);
	}

	/**
	 * Sends data about all alive cells to all players.
	 */
	private void sendAllAliveCellData() {
		CellList list = getAllAliveCellData();
		sendToPlayers(list);
	}

	/**
	 * Sends map data (width, height) to all players.
	 */
	private void sendMapData() {
		MapData mapData = getMapData();
		sendToPlayers(mapData);
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
	 * Assembles {@link CellList} from all alive cells.
	 *
	 * @return CellList
	 */
	private CellList getAllAliveCellData() {
		List<CellData> cellData = new ArrayList<>();
		for (Cell cell : grid.getAllCells().stream().filter(Cell::isAlive).collect(Collectors.toList())) {
			cellData.add(new CellData(cell));
		}
		return new CellList(cellData);
	}

	/**
	 * Assembles {@link CellList} from all cells.
	 *
	 * @return CellList
	 */
	private CellList getAllCellData() {
		List<CellData> cellData = new ArrayList<>();
		for (Cell cell : grid.getAllCells()) {
			cellData.add(new CellData(cell));
		}
		return new CellList(cellData);
	}

	/**
	 * Sends clicked cells updates to all players.
	 */
	private void sendClickedCells() {
		CellList list = constructCellList(clickedCells);
		if (!list.cells.isEmpty()) {
			sendToPlayers(list);
			clickedCells.clear();
		}
		clickedPlayers.clear();
	}

	/**
	 * Sends data about simulation steps ({@link TickData})
	 * to all players.
	 */
	private void sendSimulationSteps() {
		TickData tickData = new TickData(simulationSteps);
		sendToPlayers(tickData);
	}

	/**
	 * Sends a given message to all players in this simulation.
	 *
	 * @param message message to send
	 */
	private void sendToPlayers(OutgoingMessage message) {
		synchronized (players) {
			for (Player p : players.values()) {
				p.send(message);
			}
		}
	}

	/**
	 * Sends all CellData to all players
	 */
	private void synchronize() {
		sendToPlayers(getAllCellData());
	}

	public long getMaxPlayers() {
		return maxPlayers;
	}

	public boolean isOutOfTime() {
		return timePassed > duration;
	}

	private void updateTime() {
		if (t0 == -1) {
			t0 = System.nanoTime();
		}
		timePassed += (System.nanoTime() - t0) / 1e6;
		t0 = System.nanoTime();
	}

	public boolean isFull() {
		return getMaxPlayers() == players.size();
	}

	private final Queue<MessageQueueNode> queuedMessages = new ConcurrentLinkedQueue<>();

	public void addMessage(IncomingMessage message, long id) {
		queuedMessages.add(new MessageQueueNode(message, id));
	}

	private static class MessageQueueNode {

		private final IncomingMessage incomingMessage;
		private final long id;

		public MessageQueueNode(IncomingMessage incomingMessage, long id) {
			this.incomingMessage = incomingMessage;
			this.id = id;
		}

		public IncomingMessage getIncomingMessage() {
			return incomingMessage;
		}

		public long getId() {
			return id;
		}
	}

	private void handleMessages() {
		MessageQueueNode node;
		while ((node = queuedMessages.poll()) != null) {
			IncomingMessage message = node.getIncomingMessage();
			long id = node.getId();
			if (message.getType() == IncomingType.CLICK) {
				ClickMessage msg = (ClickMessage) message;
				click(msg.getIndices(), id);
			}
		}
	}

	public long getTickRate() {
		return tickRate;
	}

	public boolean isScheduledForRemoval() {
		return scheduledForRemoval;
	}

	private void setScheduledForRemoval(boolean scheduledForRemoval) {
		this.scheduledForRemoval = scheduledForRemoval;
	}

	/**
	 * Ends the game, disconnects all players.
	 */
	public void end() {
		synchronized (players) {
			for (Player player : players.values()) {
				player.disconnect();
			}
		}
	}

	private void sendRemainingTime() {
		TimeRemainingMessage timeRemainingMessage = new TimeRemainingMessage(duration - timePassed);
		synchronized (players) {
			for (Player player : players.values()) {
				player.send(timeRemainingMessage);
			}
		}
	}

}
