package soze.multilife.simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import soze.multilife.messages.outgoing.CellData;
import soze.multilife.messages.outgoing.CellList;
import soze.multilife.messages.outgoing.MapData;
import soze.multilife.simulation.rule.Rule;
import soze.multilife.simulation.rule.RuleFactory;
import soze.multilife.simulation.rule.RuleType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by KJurek on 21.02.2017.
 */
public class Simulation {

  private final List<Player> freshPlayers = new ArrayList<>();
  private final Map<Long, Player> players = new HashMap<>();
  private final Map<Long, String> playerColors = new HashMap<>();
  private final String[] availableColors = new String[] {"#ff0000", "#00ff00", "#0000ff", "#ffff00"};
  private final int width;
  private final int height;
  private final Cell[] cells;
  private final Cell[] newCells;
  private final Map<Long, Rule> rules = new HashMap<>();

  public Simulation(int width, int height) {
    this.width = width;
    this.height = height;
    this.cells = new Cell[width * height];
    this.newCells = new Cell[width * height];
    rules.put(0L, RuleFactory.getRule(RuleType.DIAMOEBA));
  }

  public void init() {
    for(int i = 0; i < height; i++) {
      for(int j = 0; j < width; j++) {
	Cell cell1 = new Cell(j, i);
	Cell cell2 = new Cell(j, i);
	cells[getIndex(j, i)] = cell1;
	newCells[getIndex(j, i)] = cell2;
	if(Math.random() < 0.75) {
	  cell1.setIsAlive(true);
	}
      }
    }
  }

  public void addFreshPlayer(Player player) {
    synchronized (freshPlayers) {
      freshPlayers.add(player);
    }
  }

  public void removePlayer(long id) {
    synchronized (players) {
      players.remove(id);
      playerColors.remove(id);
    }
  }

  public void update() {
    updateFreshPlayers();
    updateCells();
    sendData();
    switchCells();
  }

  public void click(int index, long id) {
    Cell cell = cells[index];
    click(cell, id);
  }

  private void click(Cell cell, long id) {
    int x = cell.getX();
    int y = cell.getY();

    Set<Cell> explodedCells = new HashSet<>();
    for(int i = -4; i < 6; i++) {
      for(int j = -4; j < 6; j++) {
	Cell c = getCellAt(x + i, y + j);
	explodedCells.add(c);
	long ownerId = c.getOwnerId();
	boolean alive = c.isAlive();
	if(ownerId == 0 && !alive) {
	  c.setOwnerId(id);
	  c.setIsAlive(true);
	}
      }
    }
  }

  private void updateFreshPlayers() {
    synchronized (freshPlayers) {
      if(!freshPlayers.isEmpty()) {

	for (Player p : freshPlayers) {
	  playerColors.put(p.getId(), availableColors[players.size()]);
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
    int maxSize = cells.length;
    if(index < 0) return index + (maxSize); // wrap around if neccesary
    if(index >= maxSize) return index % (maxSize);
    return index; // return index if not neccesary to wrap
  }

  private Cell getCellAt(int x, int y) {
    return cells[getIndex(x, y)];
  }


  private void updateCells() {
    for(int i = 0; i < cells.length; i++) {
      Cell cell = cells[i];
      List<Cell> aliveNeighbours = getAliveNeighbours(cell.getX(), cell.getY());
      long ownerId = cell.getOwnerId();
      int state = rules.get(ownerId).apply(aliveNeighbours.size(), cell.isAlive());
      if(state != 0) {
	long strongestOwnerId = getStrongestOwnerId(aliveNeighbours);
	newCells[i].setIsAlive(state > 0);
	newCells[i].setOwnerId(strongestOwnerId);
      }
    }
  }

  /**
   * Returns a list of all alive cells that are adjacent to a cell at location x, y.
   * @param x
   * @param y
   * @return
   */
  private List<Cell> getAliveNeighbours(int x, int y) {
    List<Cell> aliveNeighbours = new ArrayList<>();
    for(int i = -1; i < 2; i++) {
      for(int j = -1; j < 2; j++) {
	if(i == 0 && j == 0) continue;
	Cell cell = getCellAt(x + i, y + j);
	if(cell.isAlive()) aliveNeighbours.add(cell);
      }
    }
    return aliveNeighbours;
  }

  private long getStrongestOwnerId(List<Cell> cells) {
    List<Long> ownerIds = cells.stream().map(Cell::getOwnerId).collect(Collectors.toList());
    return mode(ownerIds);
  }

  private long mode(List<Long> list) {
    long maxValue = 0, maxCount = 0;

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
    for(int i = 0; i < newCells.length; i++) {
      cellData.add(new CellData(newCells[i]));
    }
    return new CellList(cellData);
  }

  private void switchCells() {
    for(int i = 0; i < cells.length; i++) {
      Cell cell1 = cells[i];
      Cell cell2 = newCells[i];
      cell1.setIsAlive(cell2.isAlive());
      cell1.setOwnerId(cell2.getOwnerId());
    }
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
