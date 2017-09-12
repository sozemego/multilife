package soze.multilife.metrics.events;

import soze.multilife.metrics.service.MetricsServiceImpl;

/**
 * Event based on outgoing serialized data.
 */
public class OutgoingSizeMetricEvent implements MetricEvent {

	private final long timeStamp;
	private final int connectionId;
	private final int bytesSent;

	public OutgoingSizeMetricEvent(long timeStamp, int connectionId, int bytesSent) {
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

	public void accept(MetricsServiceImpl.MetricEventVisitor visitor) {
		visitor.visit(this);
	}

}
