package soze.multilife.simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import soze.multilife.messages.outgoing.CellData;
import soze.multilife.messages.outgoing.CellList;
import soze.multilife.messages.outgoing.MapData;
import soze.multilife.simulation.rule.RuleFactory;
import soze.multilife.simulation.rule.RuleType;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * An object which is the actual simulation of cells.
 */
public class Simulation {

  /** A static cell, not located anywhere, in case we need to return invalid cell. */
  private static final Cell nullCell = new Cell(-1, -1);

  /** Players which recently joined, have not received map data yet. */
  private final List<Player> freshPlayers = new ArrayList<>();

  /** Players already in-game. */
  private final Map<Long, Player> players = new HashMap<>();

  /** Maps playerId to color (#hex) of their alive cells. */
  private final Map<Long, String> playerColors = new HashMap<>();
  /** Colors which are available for players. */
  private final String[] availableColors = new String[] {"#ff0000", "#00ff00", "#0000ff", "#ffff00"};

  /** Width and height of the game grid. */
  private final int width;
  private final int height;

  /** Objects managing the cells. It manages all cells as well as the active cells and the cells
   * that will be active in the next generation. */
  private final Grid grid;

  /** Percent of alive cells spawned on simulation start. */
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
    for(int i = 0; i < width; i++) {
      for(int j = 0; j < height; j++) {
        if(random.nextFloat() < initialDensity) {
          grid.changeState(i, j, true, simulationOwnerId);
		}
	  }
	}
  }

  /**
   * Adds a player to the simulation.
   * @param player
   */
  public void addPlayer(Player player) {
	synchronized (freshPlayers) {
	  freshPlayers.add(player);
	}
  }

  /**
   * Removes a player from the simulation.
   * @param id
   */
  public void removePlayer(long id) {
	synchronized (players) {
	  players.remove(id);
	  playerColors.remove(id);
	}
  }

  /**
   * Called when a player clicks on a grid. A player sends an array of indices
   * of cells they supposedly marked. This method goes through all of these cells
   * and if possible (dead cell) marks it as alive and sets its owner as the player who clicked.
   * @param indices
   * @param id
   */
  public void click(int[] indices, long id) {
	for(int i = 0; i < indices.length; i++) {
	  int index = indices[i];
	  grid.click(index, id);
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
	if(!players.isEmpty()) {
	  grid.update();
	  sendActiveCellData();
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
	  if(!freshPlayers.isEmpty()) {

		for (Player p : freshPlayers) {
		  playerColors.put(p.getId(), availableColors[players.size() % availableColors.length]);
		  grid.addRule(p.getId(), RuleFactory.getRule(p.getRule()));
		}

		synchronized (players) {
		  Iterator<Player> it = freshPlayers.iterator();
		  while(it.hasNext()) {
			Player player = it.next();
			players.put(player.getId(), player);
			it.remove();
		  }

		  sendMapData();
		  sendAllCellData();
		}
	  }
	}
  }

  /**
   * Assembles and stringifies MapData.
   * @return
   */
  private String getMapData() {
	ObjectMapper mapper = new ObjectMapper();
	MapData dataObject = getMapDataObject();
	try {
	  return mapper.writeValueAsString(dataObject);
	} catch (JsonProcessingException e) {
	  e.printStackTrace();
	  return "";
	}
  }

  private MapData getMapDataObject() {
	MapData mapData = new MapData(width, height, playerColors);
	return mapData;
  }

  private void sendAllCellData() {
	CellList list = getAllCellData();
	sendCellList(list);
  }

  private void sendMapData() {
	String mapData = getMapData();
	for (Player p : players.values()) {
	  p.send(mapData);
	}
  }

  /**
   * Sends next active cells to all players.
   */
  private void sendActiveCellData() {
	CellList list = getActiveCellData();
	sendCellList(list);
  }

  private void sendCellList(CellList list) {
	ObjectMapper mapper = new ObjectMapper();
	try {
	  String serializedList = mapper.writeValueAsString(list);
	  synchronized (players) {
		for (Player p : players.values()) {
		  p.send(serializedList);
		}
	  }
	} catch (JsonProcessingException e) {
	  e.printStackTrace();
	}
  }

  private CellList getAllCellData() {
	List<CellData> cellData = new ArrayList<>();
	for(Cell cell: grid.getAllCells().stream().filter(Cell::isAlive).collect(Collectors.toList())) {
	  cellData.add(new CellData(cell));
	}
	return new CellList(cellData);
  }

  private CellList getActiveCellData() {
	List<CellData> cellData = new ArrayList<>();
	for(Cell cell: grid.getActiveCells()) {
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
