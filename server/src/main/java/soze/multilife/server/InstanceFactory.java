package soze.multilife.server;

import soze.multilife.simulation.Simulation;
import soze.multilife.simulation.SimulationFactory;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * An object responsible for creating, configuring and starting new {@link Instance} objects.
 */
public class InstanceFactory {

	private final Executor executor = Executors.newCachedThreadPool();
	private final Map<Long, Instance> instances;
	private final Supplier<Integer> maxPlayers;
	private final Supplier<Long> instanceDuration;
	private final Supplier<Long> iterationInterval;
	private final SimulationFactory simulationFactory;
	private long currentId = 0;

	public InstanceFactory(Map<Long, Instance> instances,
						   Supplier<Integer> maxPlayers,
						   Supplier<Long> instanceDuration,
						   Supplier<Long> iterationInterval,
						   SimulationFactory simulationFactory) {
		this.instances = instances;
		this.maxPlayers = maxPlayers;
		this.instanceDuration = instanceDuration;
		this.iterationInterval = iterationInterval;
		this.simulationFactory = simulationFactory;
	}

	/**
	 * Finds an instance with free space, or creates a new one if unsuccesful.
	 * For each new instance, a new {@link InstanceRunner} is created and started.
	 *
	 * @return an instance
	 */
	Instance getInstance() {

		// first, try to find an instance with free space
		synchronized (instances) {
			for (Instance instance : instances.values()) {
				if (!instance.isFull() && !instance.isScheduledForRemoval()) {
					return instance;
				}
			}
		}

		//TODO simulation, max players and duration should be passed here
		// not a single instance was found, so let's create a new one.
		Simulation simulation = simulationFactory.getSimulation();
		simulation.init(); //TODO decide if this should be here
		Instance instance = new Instance(++currentId, simulation, maxPlayers.get(), instanceDuration.get());
		executor.execute(new InstanceRunner(instance, iterationInterval.get()));
		return instance;
	}


}
