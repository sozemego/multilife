package soze.multilife.metrics.events;

/**
 * Event for size data about an incoming event.
 */
public class IncomingSizeMetricEvent implements MetricEvent {

	private final long timeStamp;
	private final int connectionId;
	private final int bytesReceived;

	public IncomingSizeMetricEvent(long timeStamp, int connectionId, int bytesReceived) {
		this.timeStamp = timeStamp;
		this.connectionId = connectionId;
		this.bytesReceived = bytesReceived;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public int getConnectionId() {
		return connectionId;
	}

	public int getBytesReceived() {
		return bytesReceived;
	}


}