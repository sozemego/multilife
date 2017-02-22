package soze.multilife.simulation;

/**
 * Produces simulation objects.
 */
public class SimulationFactory {

	private final int defaultWidth = 100;
	private final int defaultHeight = 100;

	public Simulation getSimulation() {
		return new Simulation(defaultWidth, defaultHeight);
	}

}
