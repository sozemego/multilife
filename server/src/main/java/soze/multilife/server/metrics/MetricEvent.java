package soze.multilife.server.metrics;

/**
 * Event passed by {@link MetricsConnection} to {@link MetricsService}.
 */
public class MetricEvent {

  private final long timeStamp;
  private final long connectionId;
  private final int bytesSent;

  public MetricEvent(long timeStamp, long connectionId, int bytesSent) {
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

  public int getBytesSent() {
	return bytesSent;
  }
}
