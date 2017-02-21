package soze.multilife.simulation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import soze.multilife.messages.outgoing.CellData;
import soze.multilife.messages.outgoing.CellList;
import soze.multilife.simulation.rule.RuleFactory;
import soze.multilife.simulation.rule.RuleType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by KJurek on 21.02.2017.
 */
public class Simulation {

	private final int width;
	private final int height;
	private final Cell[] cells;
	private final Cell[] newCells;

	public Simulation(int width, int height) {
		this.width = width;
		this.height = height;
		this.cells = new Cell[width * height];
		this.newCells = new Cell[width * height];
	}

	public void init() {
		for(int i = 0; i < height; i++) {
			for(int j = 0; j < width; j++) {
				Cell cell1 = new Cell(j, i, RuleFactory.getRule(RuleType.BASIC));
				Cell cell2 = new Cell(j, i, RuleFactory.getRule(RuleType.BASIC));
				cells[getIndex(j, i)] = cell1;
				newCells[getIndex(j, i)] = cell2;
			}
		}
	}

	/**
	 * Returns index in an one-dimensional array based on cell coordinates.
	 * @param x
	 * @param y
	 * @return
	 */
	private int getIndex(int x, int y) {
		int index = x + (y * width); // find index
		if(index < 0) return index + (width * height); // wrap around if neccesary
		if(index >= width * height) return index % (width * height);
		return index; // return index if not neccesary to wrap
	}

	public void update(Collection<Player> players) {
		updateCells();
		sendData(players);
		switchCells();
	}

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

	private Cell getCellAt(int x, int y) {
		return cells[getIndex(x, y)];
	}

	private void updateCells() {
		for(int i = 0; i < cells.length; i++) {
			Cell cell = cells[i];
			List<Cell> aliveNeighbours = getAliveNeighbours(cell.getX(), cell.getY());
			int state = cell.getRule().apply(aliveNeighbours.size(), cell.isAlive());
			if(state != 0) {
				newCells[i].setIsAlive(state > 0);
			}
		}
	}

	/**
	 * Sends current cell data to all players.
	 * @param players
	 */
	private void sendData(Collection<Player> players) {
		CellList list = getCellData();
		ObjectMapper mapper = new ObjectMapper();
		try {
			String serializedList = mapper.writeValueAsString(list);
			for (Player p : players) {
				p.send(serializedList);
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
			cell2.setIsAlive(cell1.isAlive());
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
