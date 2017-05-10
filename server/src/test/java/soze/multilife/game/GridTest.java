package soze.multilife.game;

import org.junit.Test;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class GridTest {

	private Grid getGrid(int width, int height) {
		return new Grid(width, height);
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

		long playerId = 1L;

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

		long playerId = 1L;

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

}