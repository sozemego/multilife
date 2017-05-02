package soze.multilife.simulation;

import soze.multilife.configuration.SimulationFactoryConfiguration;

/**
 * Produces simulation objects.
 */
public class SimulationFactory {

	private final SimulationFactoryConfiguration config;

	public SimulationFactory(SimulationFactoryConfiguration config) {
		this.config = config;
	}

	public Simulation getSimulation() {
		return new Simulation(config.getGridWidth(), config.getGridHeight(), config.getMaxPlayers(), config.getGameDuration());
	}

}
