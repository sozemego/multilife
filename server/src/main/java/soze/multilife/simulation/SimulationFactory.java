package soze.multilife.simulation;

import java.util.function.Supplier;

/**
 * Produces simulation objects.
 */
public class SimulationFactory {

	private final Supplier<Integer> width;
	private final Supplier<Integer> height;

	public SimulationFactory(Supplier<Integer> width, Supplier<Integer> height) {
		this.width = width;
		this.height = height;
	}

	public Simulation getSimulation() {
		return new Simulation(width.get(), height.get());
	}

}
