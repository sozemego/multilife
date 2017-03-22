package soze.multilife.simulation;

/**
 * Produces simulation objects.
 */
public class SimulationFactory {

  private final int defaultWidth = 50;
  private final int defaultHeight = 50;

  public Simulation getSimulation() {
	return new Simulation(defaultWidth, defaultHeight);
  }

}
