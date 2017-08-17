package soze.multilife.metrics.events;

/**
 * Event based on outgoing serialized data.
 */
public class SerializedMetricEvent {

	private final long timeStamp;
	private final int connectionId;
	private final int bytesSent;

	public SerializedMetricEvent(long timeStamp, int connectionId, int bytesSent) {
		this.timeStamp = timeStamp;
		this.connectionId = connectionId;
		this.bytesSent = bytesSent;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public int getConnectionId() {
		return connectionId;
	}

	public int getBytesSent() {
		return bytesSent;
	}

}
