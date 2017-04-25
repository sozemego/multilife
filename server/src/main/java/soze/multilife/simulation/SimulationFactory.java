package soze.multilife.simulation;

/**
 * Produces simulation objects.
 */
public class SimulationFactory {

	private final int width;
	private final int height;

	public SimulationFactory(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Simulation getSimulation() {
		return new Simulation(width, height);
	}

}
