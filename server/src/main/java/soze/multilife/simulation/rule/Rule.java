package soze.multilife.simulation.rule;

public interface Rule {

  /**
   * All implementing classes should consider the number of alive neighbours
   * and the current state of the cell (alive, dead), to determine whether it lives
   * or dies (-1 means the cell dies, 1 means the cell comes alive, 0 means cell stays the same).
   *
   * @param aliveNeighbours
   * @param alive
   * @return
   */
  public int apply(int aliveNeighbours, boolean alive);

}
