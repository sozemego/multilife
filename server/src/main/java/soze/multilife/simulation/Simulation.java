package soze.multilife.simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import soze.multilife.messages.outgoing.CellData;
import soze.multilife.messages.outgoing.CellList;
import soze.multilife.messages.outgoing.MapData;
import soze.multilife.simulation.rule.Rule;
import soze.multilife.simulation.rule.RuleFactory;
import soze.multilife.simulation.rule.RuleType;

import java.awt.*;
import java.util.*;
import java.util.List;
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

  /** Arrays of current cells and next iteration cells. */
  //private final Cell[] cells;
  //private final Cell[] newCells;

	private final Grid currentGrid = new Grid();

  private final Map<Point, Cell> currentCells = new HashMap<>();
  private final Map<Point, Cell> nextCells = new HashMap<>();
  private final Set<Cell> currentActiveCells = new HashSet<>();
  private final Set<Cell> nextActiveCells = new HashSet<>();

  /** Percent of alive cells spawned on simulation start. */
  private final float initialDensity = 0.5f;

  /** Maps playerId to game of life rule they use. */
  private final Map<Long, Rule> rules = new HashMap<>();

  private final long simulationOwnerId = 0L;

  public Simulation(int width, int height) {
	this.width = width;
	this.height = height;
	rules.put(simulationOwnerId, RuleFactory.getRule(RuleType.BASIC));
  }

  /**
   * Spawns cells for the entire game grid.
   */
  public void init() {
	for(int i = 0; i < height; i++) {
	  for(int j = 0; j < width; j++) {
		if(Math.random() < initialDensity) {
		  currentGrid.addCell(j, i);
		}
	  }
	}

	for(int i = 0; i < allCells.length; i++) {
	  if(Math.random() < initialDensity) {
		Cell cell = allCells[i];
		cell.setIsAlive(true);
		currentActiveCells.add(cell);
		currentActiveCells.addAll(getAllNeighbours(cell));
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
	  Cell cell = getCellAt(index);
	  if(!cell.isAlive()) {
		cell.setIsAlive(true);
		cell.setOwnerId(id);
	  }
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
	  updateCells();
	  sendData();
	  switchCells();
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
		  rules.put(p.getId(), RuleFactory.getRule(p.getRule()));
		}

		synchronized (players) {
		  Iterator<Player> it = freshPlayers.iterator();
		  while(it.hasNext()) {
			Player player = it.next();
			players.put(player.getId(), player);
			it.remove();
		  }

		  String mapData = getMapData();
		  for (Player p : players.values()) {
			p.send(mapData);
		  }
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

  /**
   * Returns index in an one-dimensional array based on cell coordinates.
   * @param x
   * @param y
   * @return
   */
  private int getIndex(int x, int y) {
	int index = x + (y * width); // find index
	int maxSize = allCells.length;
	if(index < 0) return index + (maxSize); // wrap around if neccesary
	if(index >= maxSize) return index % (maxSize);
	return index; // return index if not neccesary to wrap
  }

  /**
   * Returns a cell at given coordinates.
   * @param x
   * @param y
   * @return
   */
  private Cell getCellAt(int x, int y) {
	return getCellAt(getIndex(x, y));
  }

  private Cell getCellAt(int index) {
	return (index < 0 || index >= allCells.length) ? nullCell : allCells[index];
  }

  /**
   * Iterates all cells and based on their and their neighbours state
   * updates newCells grid.
   */
  private void updateCells() {
	for(Cell cell: currentActiveCells) {
	  List<Cell> aliveNeighbours = getAliveNeighbours(cell);
	  long ownerId = cell.getOwnerId();
	  int state = rules.get(ownerId).apply(aliveNeighbours.size(), cell.isAlive());
	  if(state != 0) {
		long strongestOwnerId = getStrongestOwnerId(aliveNeighbours);
		Cell nextCell = new Cell(cell.getX(), cell.getY());
		nextCell.setIsAlive(state > 0);
		nextCell.setOwnerId(strongestOwnerId);
		nextActiveCells.add(nextCell);
		nextActiveCells.addAll(cell.getNeighbours());
	  }
	}


//		for(int i = 0; i < cells.length; i++) {
//			Cell cell = cells[i];
//			List<Cell> aliveNeighbours = getAliveNeighbours(cell.getX(), cell.getY());
//			long ownerId = cell.getOwnerId();
//			int state = rules.get(ownerId).apply(aliveNeighbours.size(), cell.isAlive());
//			if(state != 0) {
//				long strongestOwnerId = getStrongestOwnerId(aliveNeighbours);
//				newCells[i].setIsAlive(state > 0);
//				newCells[i].setOwnerId(strongestOwnerId);
//			}
//		}
  }

  /**
   * Returns a list of all alive cells that are adjacent to a cell at location x, y.
   * @return
   */
  private List<Cell> getAliveNeighbours(Cell cell) {
	return getAllNeighbours(cell).stream()
	  .filter(Cell::isAlive)
	  .collect(Collectors.toList());
  }

  private List<Cell> getAllNeighbours(Cell cell) {
	return getAllNeighbours(cell.getX(), cell.getY());
  }

  /**
   * Finds all neighbours of a cell at x, y position.
   * @param x
   * @param y
   * @return
   */
  private List<Cell> getAllNeighbours(int x, int y) {
	List<Cell> neighbours = new ArrayList<>();
	for(int i = -1; i < 2; i++) {
	  for(int j = -1; j < 2; j++) {
		if(i == 0 && j == 0) continue;
		neighbours.add(getCellAt(x + i, y + j));
	  }
	}
	return neighbours;
  }

  private Cell copyCell(Cell cell) {
	Cell newCell = new Cell(cell.getX(), cell.getY());
	newCell.setIsAlive(cell.isAlive());
	newCell.setOwnerId(cell.getOwnerId());
	return;
  }

  /**
   * Finds the most frequently (mode) occuring ownerId among given cells.
   * @param cells
   * @return
   */
  private long getStrongestOwnerId(List<Cell> cells) {
	List<Long> ownerIds = cells.stream().map(Cell::getOwnerId).collect(Collectors.toList());
	return mode(ownerIds);
  }

  private long mode(List<Long> list) {
	long maxValue = simulationOwnerId, maxCount = 0;

	for (int i = 0; i < list.size(); ++i) {
	  int count = 0;
	  for (int j = 0; j < list.size(); ++j) {
		if (list.get(j).equals(list.get(i))) ++count;
	  }
	  if (count > maxCount) {
		maxCount = count;
		maxValue = list.get(i);
	  }
	}

	return maxValue;
  }

  /**
   * Sends current cell data to all players.
   */
  private void sendData() {
	CellList list = getCellData();
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

  private CellList getCellData() {
	List<CellData> cellData = new ArrayList<>();
	for(Cell cell: nextActiveCells) {
	  cellData.add(new CellData(cell));
	}
	return new CellList(cellData);
  }

  /**
   * Switches current and new grid.
   */
  private void switchCells() {
	currentActiveCells.clear();
	currentActiveCells.addAll(nextActiveCells);
	nextActiveCells.clear();

//		for(int i = 0; i < cells.length; i++) {
//			Cell cell1 = cells[i];
//			Cell cell2 = newCells[i];
//			cell1.setIsAlive(cell2.isAlive());
//			cell1.setOwnerId(cell2.getOwnerId());
//		}
  }

  public int getWidth() {
	return width;
  }

  public int getHeight() {
	return height;
  }
}
