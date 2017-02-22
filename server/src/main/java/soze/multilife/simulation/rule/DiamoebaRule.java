package soze.multilife.simulation.rule;

/**
 * Created by KJurek on 22.02.2017.
 */
public class DiamoebaRule implements Rule {

  @Override
  public int apply(int aliveNeighbours, boolean alive) {
    if(alive) {
      if(aliveNeighbours < 5 || aliveNeighbours > 8) {
        return -1;
      }
    } else {
      if(aliveNeighbours == 3 || (aliveNeighbours > 4 && aliveNeighbours < 9)) {
        return 1;
      }
    }
    return 0;
  }
}
