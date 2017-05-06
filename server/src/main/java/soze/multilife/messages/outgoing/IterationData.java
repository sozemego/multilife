package soze.multilife.messages.outgoing;

/**
 * Contains information about the current simulation tick.
 */
public class IterationData extends OutgoingMessage {

	public long iterations;

	public IterationData(long iterations) {
		setType(OutgoingType.TICK_DATA);
		this.iterations = iterations;
	}
}
