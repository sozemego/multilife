package soze.multilife.messages.outgoing;

/**
 * Contains information about the current simulation tick.
 */
public class TickData extends OutgoingMessage {

  public long simulationSteps;

  public TickData(long simulationSteps) {
    setType(OutgoingType.TICK_DATA);
	this.simulationSteps = simulationSteps;
  }
}
