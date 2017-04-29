package soze.multilife.server.metrics.events;

/**
 * Event constructed based on the instance of outgoing data.
 *
 * @see soze.multilife.messages.outgoing.OutgoingMessage
 */
public class TypeMetricEvent {

	private final long timestamp;
	private final String type;
	private final long connectionId;

	public TypeMetricEvent(long timestamp, String type, long connectionId) {
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

	public long getConnectionId() {
		return connectionId;
	}
}
