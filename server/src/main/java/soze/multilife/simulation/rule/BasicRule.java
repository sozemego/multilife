package soze.multilife.simulation.rule;

/**
 * Created by soze on 2/21/2017.
 */
public class BasicRule implements Rule {

	@Override
	public int apply(int aliveNeighbours, boolean alive) {
		if(alive) {
			if(aliveNeighbours < 2 || aliveNeighbours > 3) {
				return -1;
			}
		} else {
			if(aliveNeighbours == 3) {
				return 1;
			}
		}
		return 0;
	}


}
