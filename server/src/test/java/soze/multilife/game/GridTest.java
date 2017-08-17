package soze.multilife.game;

import org.junit.Test;
import soze.multilife.game.rule.RuleFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 *
 */
public class GridTest {

	private Grid getGrid(int width, int height) {
		return new Grid(width, height, RuleFactory.getRule("BASIC"));
	}

	private Grid getDefaultGrid() {
		return getGrid(25, 25);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInitGridIllegalDimensions() throws Exception {
		getGrid(-25, 0);
	}

	@Test
	public void testChangeState() throws Exception {
		Grid grid = getDefaultGrid();
		List<Point> pointsToSetAlive = Arrays.asList(new Point(0, 1), new Point(0, 5));

		int playerId = 1;

		for(Point p: pointsToSetAlive) {
			grid.changeState(p.x, p.y, true, playerId);
		}

		grid.updateGrid();

		List<Cell> aliveCells = grid.getAllCells()
				.values()
				.stream()
				.filter(Cell::isAlive)
				.collect(Collectors.toList());
		assertEquals(aliveCells.size(), 2);
		assertTrue(aliveCells.stream()
				.map(c -> new Point(c.getX(), c.getY()))
				.collect(Collectors.toList())
				.containsAll(pointsToSetAlive)
		);
	}

	@Test
	public void testKillAll() throws Exception {
		Grid grid = getDefaultGrid();
		List<Point> pointsToSetAlive = Arrays.asList(new Point(0, 1), new Point(0, 5));

		int playerId = 1;

		for(Point p: pointsToSetAlive) {
			grid.changeState(p.x, p.y, true, playerId);
		}

		grid.updateGrid();

		List<Cell> aliveCells = grid.getAllCells()
				.values()
				.stream()
				.filter(Cell::isAlive)
				.collect(Collectors.toList());
		assertEquals(aliveCells.size(), 2);
		assertTrue(aliveCells.stream()
				.map(c -> new Point(c.getX(), c.getY()))
				.collect(Collectors.toList())
				.containsAll(pointsToSetAlive));

		grid.killAll(playerId);

		aliveCells = grid.getAllCells()
				.values()
				.stream()
				.filter(Cell::isAlive)
				.collect(Collectors.toList());
		assertEquals(aliveCells.size(), 0);
		assertFalse(aliveCells.stream()
				.map(c -> new Point(c.getX(), c.getY()))
				.collect(Collectors.toList())
				.containsAll(pointsToSetAlive));
	}

	@Test
	public void testGridUpdate() throws Exception {
		Grid grid = getGrid(10, 10);
		List<Point> blinker = new ArrayList<>();
		blinker.add(new Point(5, 5));
		blinker.add(new Point(5, 6));
		blinker.add(new Point(5, 7));
		for(Point p: blinker) {
			grid.changeState(p.x, p.y, true, 1);
		}
		grid.updateGrid();
		grid.updateGrid();
		Map<Point, Cell> cells = grid.getAllCells();
		assertTrue(cells.get(new Point(4, 6)).isAlive());
		assertTrue(cells.get(new Point(5, 6)).isAlive());
		assertTrue(cells.get(new Point(6, 6)).isAlive());
	}

	@Test
	public void testGridUpdateAllCellsShouldDie() throws Exception {
		Grid grid = getGrid(10, 10);
		for(int i = 0; i < grid.getWidth(); i++) {
			for(int j = 0; j < grid.getHeight(); j++) {
				grid.changeState(i, j, true, 1);
			}
		}
		grid.updateGrid();
		Map<Point, Cell> allCells = grid.getAllCells();
		for(Cell cell: allCells.values()) {
			assertTrue(cell.isAlive());
		}
		grid.updateGrid();
		allCells = grid.getAllCells();
		for(Cell cell: allCells.values()) {
			assertFalse(cell.isAlive());
		}
	}

}