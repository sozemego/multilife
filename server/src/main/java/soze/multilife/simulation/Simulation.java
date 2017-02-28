package soze.multilife.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.messages.outgoing.CellData;
import soze.multilife.messages.outgoing.CellList;
import soze.multilife.messages.outgoing.MapData;
import soze.multilife.messages.outgoing.PlayerData;
import soze.multilife.simulation.rule.RuleFactory;
import soze.multilife.simulation.rule.RuleType;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An object which is the actual simulation of cells.
 */
public class Simulation {

  private static final Logger LOG = LoggerFactory.getLogger(Simulation.class);

  /**
   * A static cell, not located anywhere, in case we need to return invalid cell.
   */
  private static final Cell nullCell = new Cell(-1, -1);

  /**
   * Players which recently joined, have not received map data yet.
   */
  private final List<Player> freshPlayers = new ArrayList<>();

  /**
   * Players already in-game.
   */
  private final Map<Long, Player> players = new HashMap<>();

  /**
   * All players ever in this instance.
   */

  private final Map<Long, Player> allPlayers = new HashMap<>();

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
   * Objects managing the cells. It manages all cells as well as the active cells and the cells
   * that will be active in the next generation.
   */
  private final Grid grid;

  /**
   * Percent of alive cells spawned on simulation start.
   */
  private final float initialDensity = 0.05f;

  private final long simulationOwnerId = 0L;

  public Simulation(int width, int height) {
	this.width = width;
	this.height = height;
	this.grid = new Grid(width, height);
	grid.addRule(simulationOwnerId, RuleFactory.getRule(RuleType.BASIC));
  }

  /**
   * Spawns cells for the entire game grid.
   */
  public void init() {
	SecureRandom random = new SecureRandom();
	for (int i = 0; i < width; i++) {
	  for (int j = 0; j < height; j++) {
		if (random.nextFloat() < initialDensity) {
		  grid.changeState(i, j, true, simulationOwnerId);
		}
	  }
	}
	grid.updateGrid();
  }

  /**
   * Adds a player to the simulation.
   *
   * @param player
   */
  public void addPlayer(Player player) {
	synchronized (freshPlayers) {
	  freshPlayers.add(player);
	}
  }

  /**
   * Removes a player from the simulation.
   *
   * @param id
   */
  public void removePlayer(long id) {
	synchronized (players) {
	  players.remove(id);
	}
  }

  /**
   * When a player sends an array of indices of cells they clicked,
   * this method checks if all of them are currently not alive.
   * If so, marks them as alive, sets their owner as the player who clicked
   * and sends information to all players about newly alive cells.
   * @param indices
   * @param id
   */
  public void click(int[] indices, long id) {
    LOG.trace("Player [{}] wants to click on [{}] cells.", id, indices.length);
    List<Cell> clickableCells = grid.findClickableCells(indices, id);
    if(clickableCells.size() == indices.length) {
		grid.click(clickableCells);
		sendCellList(constructCellList(clickableCells));
	}
  }

  /**
   * Updates the simulation. It sends data for newly logged in players,
   * advances the simulation by one iteration, sends data to players
   * and switches grids.
   */
  //TODO think about moving all network related stuff
  // up to Instance object. Instance would
  // ask simulation (getters) for data to send.
  public void update() {
	updateFreshPlayers();
	if (!players.isEmpty()) {
	  grid.update();
	  //sendActiveCellData(); //TODO send updates that players made
	  grid.transferCells();
	}
  }

  /**
   * Processes players who recently joined.
   * Assigns them a color and loads their game of life rule.
   * It then adds them to the players collection, and updates all players
   * with map data.
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
			allPlayers.put(player.getId(), player);
			it.remove();
		  }

		  sendPlayerData();
		  sendMapData();
		  sendAllCellData();
		}
	  }
	}
  }

  /**
   * Sends data about all players in this instance.
   */
  private void sendPlayerData() {
	PlayerData data = createPlayerData();
	for(Player player: players.values()) {
	  player.send(data);
	}
  }

  private PlayerData createPlayerData() {
    Map<Long, Long> points = new HashMap<>();
	Map<Long, String> names = new HashMap<>();
	Map<Long, String> colors = new HashMap<>();
	Map<Long, String> rules = new HashMap<>();

	// data for the simulation
	points.put(0L, 0L);
	names.put(0L, "AI");
	colors.put(0L, "#000000");
	rules.put(0L, "BASIC");

	for(Player player: allPlayers.values()) {
	  points.put(player.getId(), 0L);
	  names.put(player.getId(), player.getName());
	  colors.put(player.getId(), playerColors.get(player.getId()));
	  rules.put(player.getId(), player.getRule());
	}
	return new PlayerData(points, names, colors, rules);
  }

  /**
   * Assembles and returns MapData.
   *
   * @return
   */
  private MapData getMapData() {
	return new MapData(width, height);
  }

  private void sendAllCellData() {
	CellList list = getAllCellData();
	sendCellList(list);
  }

  private void sendMapData() {
	MapData mapData = getMapData();
	for (Player p : players.values()) {
	  p.send(mapData);
	}
  }

  private void sendCellList(CellList list) {
	synchronized (players) {
	  for (Player p : players.values()) {
		p.send(list);
	  }
	}
  }

  private CellList constructCellList(List<Cell> cells) {
    List<CellData> cellData = new ArrayList<>();
    for(Cell cell: cells) {
      cellData.add(new CellData(cell));
	}
	return new CellList(cellData);
  }

  private CellList getAllCellData() {
	List<CellData> cellData = new ArrayList<>();
	for (Cell cell : grid.getAllCells().stream().filter(Cell::isAlive).collect(Collectors.toList())) {
	  cellData.add(new CellData(cell));
	}
	return new CellList(cellData);
  }

  public int getWidth() {
	return width;
  }

  public int getHeight() {
	return height;
  }
}
