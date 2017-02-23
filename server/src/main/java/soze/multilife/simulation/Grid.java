package soze.multilife.simulation;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by KJurek on 23.02.2017.
 */
public class Grid {

  private final Map<Point, Cell> cells = new HashMap<>();

  public Grid() {

  }

  public void addCell(int x, int y, boolean populate) {
    Point p = new Point(x, y);
    cells.putIfAbsent(p, new Cell(x, y));
    if(populate) {
      populateNeighbours(x, y);
	}
  }

  private void populateNeighbours(int x, int y) {
	for(int i = -1; i < 2; i++) {
	  for(int j = -1; j < 2; j++) {
		if(i == 0 && j == 0) continue;
		addCell(x + i, y + j, false);
	  }
	}
  }

}
