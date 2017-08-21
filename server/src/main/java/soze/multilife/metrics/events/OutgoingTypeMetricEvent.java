package soze.multilife.metrics.events;

/**
 * Event constructed based on the instance of outgoing data.
 *
 * @see soze.multilife.messages.outgoing.OutgoingMessage
 */
public class OutgoingTypeMetricEvent implements MetricEvent {

	private final long timestamp;
	private final String type;
	private final int connectionId;

	public OutgoingTypeMetricEvent(long timestamp, String type, int connectionId) {
		this.timestamp = timestamp;
		this.type = type;
		this.connectionId = connectionId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getType() {
		return type;
	}

	public int getConnectionId() {
		return connectionId;
	}
}
