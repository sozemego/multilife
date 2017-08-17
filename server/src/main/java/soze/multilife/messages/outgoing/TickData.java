package soze.multilife.messages.outgoing;

/**
 * Contains information about the current simulation tick.
 */
public class TickData extends OutgoingMessage {

	public int iterations;

	public TickData(int iterations) {
		setType(OutgoingType.TICK_DATA);
		this.iterations = iterations;
	}
}
