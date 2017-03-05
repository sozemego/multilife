package soze.multilife.server;

import soze.multilife.simulation.Simulation;
import soze.multilife.simulation.SimulationFactory;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * An object responsible for creating, configuring and starting new {@link Instance} objects.
 */
class InstanceFactory {

  private final Executor executor = Executors.newCachedThreadPool();
  private final Map<Long, Instance> instances;
  private final int maxPlayers = 4;
  private final SimulationFactory simulationFactory = new SimulationFactory();
  private long currentId = 0;

  InstanceFactory(Map<Long, Instance> instances) {
	this.instances = instances;
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

	// not a single instance was found, so let's create a new one.
	Simulation simulation = simulationFactory.getSimulation();
	simulation.init(); //TODO decide if this should be here
	Instance instance = new Instance(++currentId, simulation, maxPlayers);
	executor.execute(new InstanceRunner(instance));
	return instance;
  }


}
