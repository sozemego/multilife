package soze.multilife.server;

import soze.multilife.simulation.Simulation;
import soze.multilife.simulation.SimulationFactory;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by soze on 2/21/2017.
 */
public class InstanceFactory {

	private final Executor executor = Executors.newFixedThreadPool(4);
	private final Map<Long, Instance> instances;
	private final int maxPlayers = 4;
	private final SimulationFactory simulationFactory = new SimulationFactory();
	private long currentId = 0;

	public InstanceFactory(Map<Long, Instance> instances) {
		this.instances = instances;
	}

	/**
	 * Finds an instance with free space, or creates a new one if unsuccesful.
	 * For each new instance, a new {@link InstanceRunner} is created and started.
	 * @return
	 */
	public Instance getInstance() {

		// first, try to find an instance with free space
		for(Instance instance: instances.values()) {
			if(!instance.isFull()) {
				return instance;
			}
		}

		// not a single instance was found, so let's create a new one.
		Simulation simulation = simulationFactory.getSimulation();
		simulation.init();
		Instance instance = new Instance(++currentId, simulation, maxPlayers);
		executor.execute(new InstanceRunner(instance));
		return instance;
	}


}
