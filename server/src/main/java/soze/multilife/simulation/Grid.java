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

  /**
   * This map contains all cells.
   */
  private final Map<Point, Cell> cells = new HashMap<>();
  /**
   * This map contains current active cells. During a grid update,
   * only cells contained within this collection are checked.
   */
  private final Map<Point, Cell> activeCells = new HashMap<>();
  /**
   * This map contains cells which will become active cells
   * in the next iteration.
   */
  private final Map<Point, Cell> nextCells = new HashMap<>();

  /**
   * Contains rules for players.
   */
  private final Map<Long, Rule> rules = new HashMap<>();
  private final int width;
  private final int height;

  Grid(int width, int height) {
	this.width = width;
	this.height = height;
	init();
  }

  /**
   * Populates the grid of all cells.
   */
  private void init() {
	for (int i = 0; i < width; i++) {
	  for (int j = 0; j < height; j++) {
		cells.put(new Point(i, j), new Cell(i, j));
	  }
	}
  }

  /**
   * Adds a rule for a given player.
   *
   * @param ownerId id of the player this rule should apply to
   * @param rule    rule to apply to cells. Cannot be null.
   */
  void addRule(long ownerId, Rule rule) {
	rules.put(ownerId, Objects.requireNonNull(rule));
  }

  /**
   * @return all cells in this grid
   */
  Collection<Cell> getAllCells() {
	return cells.values();
  }

  /**
   * Changes state of a cell at location x, y.
   *
   * @param x       x location of the cell
   * @param y       y location of the cell
   * @param state   state of the cell
   * @param ownerId owner of the cell
   */
  void changeState(int x, int y, boolean state, long ownerId) {
	Point p = getPoint(x, y);
	changeState(p, state, ownerId);
  }


  /**
   * Adds all given cells to the next cells.
   * @param cells
   */
  void click(Collection<Cell> cells) {
	for(Cell cell: cells) {
	  nextCells.put(new Point(cell.getX(), cell.getY()), cell);
	}
  }

  /**
   * Finds all cells that are clickable by this player.
   * @param indices array of indices of cells
   * @param ownerId id of the player who clicked
   * @return a list of all clickable (by this player) \s
   */
  List<Cell> findClickableCells(int[] indices, long ownerId) {
    List<Cell> clickableCells = new ArrayList<>();
	for(int i: indices) {
	  Point p = getPoint(i);
	  Cell cell = cells.get(p);
	  if(!cell.isAlive()) {
		cell = new Cell(p.x, p.y);
		cell.setIsAlive(true);
		cell.setOwnerId(ownerId);
		clickableCells.add(cell);
	  }
	}
	return clickableCells;
  }

  /**
   * Changes the state of a cell at a given point.
   *
   * @param p point containing coordinates
   * @param state alive/dead
   * @param ownerId id of the owner
   */
  private void changeState(Point p, boolean state, long ownerId) {
	Cell cell = nextCells.get(p);
	if (cell == null) {
	  cell = new Cell(p.x, p.y);
	  nextCells.put(p, cell);
	}
	cell.setIsAlive(state);
	cell.setOwnerId(ownerId);
  }

  /**
   * Adds this cell to the map of active cells.
   * This method also adds neighbours of this cell to active cells.
   * If cells already exist, cells are not replaced.
   *
   * @param cell
   */
  private void addToActive(Cell cell) {
	int x = cell.getX();
	int y = cell.getY();
	for (int i = -1; i < 2; i++) {
	  for (int j = -1; j < 2; j++) {
		Point p = getPoint(i + x, j + y);
		activeCells.putIfAbsent(p, getCell(p.x, p.y));
	  }
	}
  }

  /**
   * Returns a cell at this position. If x or y is out of bounds,
   * this method will wrap around the grid.
   *
   * @param x
   * @param y
   * @return cell at given coordinates (from all cells)
   */
  private Cell getCell(int x, int y) {
	return cells.get(getPoint(x, y));
  }

  /**
   * Updates the cells and transfers the cells from nextCells to active cells
   * and updates the underlying map.
   */
  void updateGrid() {
    update();
    transferCells();
  }

  /**
   * Goes through active cells and populates next iteration
   * active cells.
   */
  private void update() {
	for (Cell cell : activeCells.values()) {
	  int x = cell.getX();
	  int y = cell.getY();
	  List<Cell> aliveNeighbours = getAliveNeighbourCells(x, y);
	  long ownerId = cell.getOwnerId();
	  int state = rules.get(ownerId).apply(aliveNeighbours.size(), cell.isAlive());
	  if (state != 0) {
		long strongestOwnerId = getStrongestOwnerId(aliveNeighbours);
		changeState(x, y, state > 0, strongestOwnerId == -1 ? cell.getOwnerId() : strongestOwnerId);
	  }
	}
  }

  /**
   * Returns all alive cells around a cell at location x, y.
   *
   * @param x
   * @param y
   * @return
   */
  private List<Cell> getAliveNeighbourCells(int x, int y) {
	List<Cell> aliveNeighbourCells = new ArrayList<>();
	for (int i = -1; i < 2; i++) {
	  for (int j = -1; j < 2; j++) {
		if (i == 0 && j == 0) continue;
		Cell cell = getCell(x + i, y + j);
		if (cell.isAlive()) aliveNeighbourCells.add(cell);
	  }
	}
	return aliveNeighbourCells;
  }

  /**
   * Finds the most frequently (mode) occuring ownerId among given cells.
   * If there are no cells, returns -1.
   * @param cells
   * @return
   */
  private long getStrongestOwnerId(List<Cell> cells) {
    if(cells.isEmpty()) {
      return -1;
	}
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
   * Switches active cells and next iteration active cells.
   * After the switch, next iteration cells are cleared.
   */
  void transferCells() {
	activeCells.clear();
	for (Cell nextCell : nextCells.values()) {
	  Cell cell = getCell(nextCell.getX(), nextCell.getY());
	  cell.setIsAlive(nextCell.isAlive());
	  cell.setOwnerId(nextCell.getOwnerId());
	  addToActive(cell);
	}
	nextCells.clear();
  }

  /**
   * Creates a point object with a given x, y values.
   * If the point lies out of bounds then x, y values
   * are wrapped around.
   *
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
	return wrapIndex(index); // wrap it
  }

  private int wrapIndex(int index) {
	int maxSize = width * height;
	if (index < 0) return index + (maxSize); // wrap around if neccesary
	if (index >= maxSize) return index % (maxSize);
	return index; // return index if not neccesary to wrap
  }

  /**
   * Returns x, y coordinates of a cell at a given index,
   * as if the cells were contained in an 1D array.
   * The coordinates are returned in a Point object.
   *
   * @param index
   * @return
   */
  private Point getPoint(int index) {
    index = wrapIndex(index);
	return new Point(index % width, index / height);
  }


}
