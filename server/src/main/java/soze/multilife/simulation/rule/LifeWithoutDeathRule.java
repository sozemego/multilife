package soze.multilife.simulation.rule;

/**
 * Created by KJurek on 22.02.2017.
 */
public class LifeWithoutDeathRule implements Rule {

  @Override
  public int apply(int aliveNeighbours, boolean alive) {
    if(aliveNeighbours == 3) {
      return 1;
    }
    return 0;
  }
}
