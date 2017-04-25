package soze.multilife.server.metrics.events;

/**
 * Event based on outgoing serialized data.
 */
public class SerializedMetricEvent {

	private final long timeStamp;
	private final long connectionId;
	private final long bytesSent;

	public SerializedMetricEvent(long timeStamp, long connectionId, long bytesSent) {
		this.timeStamp = timeStamp;
		this.connectionId = connectionId;
		this.bytesSent = bytesSent;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public long getConnectionId() {
		return connectionId;
	}

	public long getBytesSent() {
		return bytesSent;
	}

}
