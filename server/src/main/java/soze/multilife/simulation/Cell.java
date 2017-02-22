package soze.multilife.simulation;

import soze.multilife.simulation.rule.Rule;

/**
 * Created by soze on 2/21/2017.
 */
public class Cell {

  private final int x;
  private final int y;
  private boolean alive = false;
  private long ownerId = 0;

  public Cell(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public boolean isAlive() {
    return alive;
  }

  public void setIsAlive(boolean alive) {
    this.alive = alive;
  }

  public long getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(long ownerId) {
    this.ownerId = ownerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Cell cell = (Cell) o;

    if (x != cell.x) return false;
    return y == cell.y;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }
}
