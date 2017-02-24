package soze.multilife.simulation;

import soze.multilife.simulation.rule.Rule;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A grid of cells. This class manages the cells, lets the simulation
 * set cell states.
 */
public class Grid {

  private final Map<Point, Cell> cells = new HashMap<>();
  private final Map<Point, Cell> activeCells = new HashMap<>();
  private final Map<Point, Cell> nextCells = new HashMap<>();
  private final Map<Long, Rule> rules = new HashMap<>();
  private final int width;
  private final int height;

  public Grid(int width, int height) {
	this.width = width;
	this.height = height;
	init();
  }

  public void addRule(long ownerId, Rule rule) {
    rules.put(ownerId, rule);
  }

  public Collection<Cell> getActiveCells() {
    return nextCells.values();
  }

  public Collection<Cell> getAllCells() {
    return cells.values();
  }

  private void init() {
    for(int i = 0; i < width; i++) {
      for(int j = 0; j < height; j++) {
        cells.put(new Point(i, j), new Cell(i, j));
	  }
	}
  }

  /**
   * Changes state of a cell at location x, y.
   * @param x
   * @param y
   * @param state
   * @param ownerId
   */
  public void changeState(int x, int y, boolean state, long ownerId) {
    Point p = getPoint(x, y);
    Cell cell = nextCells.get(p);
	if(cell == null) {
	  cell = new Cell(p.x, p.y);
	  nextCells.put(p, cell);
	}
	cell.setIsAlive(state);
	cell.setOwnerId(ownerId);
  }

  public void transferCells() {
	activeCells.clear();
    for(Cell nextCell: nextCells.values()) {
      Cell cell = getCell(nextCell.getX(), nextCell.getY());
      cell.setIsAlive(nextCell.isAlive());
      cell.setOwnerId(nextCell.getOwnerId());
      addToActive(cell);
	}
    nextCells.clear();
  }

  private void addToActive(Cell cell) {
    int x = cell.getX();
    int y = cell.getY();
	for(int i = -1; i < 2; i++) {
	  for(int j = -1; j < 2; j++) {
		Point p = getPoint(i + x, j + y);
		activeCells.putIfAbsent(p, getCell(p.x, p.y));
	  }
	}
  }

  private Cell getCell(int x, int y) {
    return cells.get(getPoint(x, y));
  }

  public void update() {
    for(Cell cell: activeCells.values()) {
	  int x = cell.getX();
	  int y = cell.getY();
	  List<Cell> aliveNeighbours = getAliveNeighbourCells(x, y);
	  long ownerId = cell.getOwnerId();
	  int state = rules.get(ownerId).apply(aliveNeighbours.size(), cell.isAlive());
	  if (state != 0) {
		long strongestOwnerId = getStrongestOwnerId(aliveNeighbours);
		changeState(x, y, state > 0, strongestOwnerId);
	  }
	}
  }

  private List<Cell> getAliveNeighbourCells(int x, int y) {
    List<Cell> aliveNeighbourCells = new ArrayList<>();
	for(int i = -1; i < 2; i++) {
	  for(int j = -1; j < 2; j++) {
		if(i == 0 && j == 0) continue;
		Cell cell = getCell(x + i, y + j);
		if(cell.isAlive()) aliveNeighbourCells.add(cell);
	  }
	}
	return aliveNeighbourCells;
  }

  /**
   * Finds the most frequently (mode) occuring ownerId among given cells.
   * @param cells
   * @return
   */
  private long getStrongestOwnerId(List<Cell> cells) {
	List<Long> ownerIds = cells.stream()
	  .map(Cell::getOwnerId)
	  .collect(Collectors.toList());
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
   * Creats a point object with a given x, y values.
   * If the point lies out of bounds then x, y values
   * are wrapped around.
   * @return
   */
  private Point getPoint(int x, int y) {
	int index = getIndex(x, y);
	int wrappedX = index % width;
	int wrappedY = index / width;
	return new Point(wrappedX, wrappedY);
  }

  private int getIndex(int x, int y) {
	int index = x + (y * width); // find index
	int maxSize = width * height;
	if(index < 0) return index + (maxSize); // wrap around if neccesary
	if(index >= maxSize) return index % (maxSize);
	return index; // return index if not neccesary to wrap
  }

}
