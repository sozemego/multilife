package soze.multilife.simulation;

/**
 * Produces simulation objects.
 */
public class SimulationFactory {

  private final int defaultWidth = 125;
  private final int defaultHeight = 125;

  public Simulation getSimulation() {
	return new Simulation(defaultWidth, defaultHeight);
  }

}
