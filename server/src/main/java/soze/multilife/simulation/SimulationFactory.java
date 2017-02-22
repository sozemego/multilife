package soze.multilife.simulation;

import java.util.Map;

/**
 * Created by soze on 2/21/2017.
 */
public class SimulationFactory {

	private final int defaultWidth = 50;
	private final int defaultHeight = 50;

	public Simulation getSimulation() {
		return new Simulation(defaultWidth, defaultHeight);
	}

}
