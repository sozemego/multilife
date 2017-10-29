package soze.multilife.metrics.events;

import soze.multilife.metrics.service.MetricsServiceImpl;

/**
 * Information about type of an incoming message.
 */
public class IncomingTypeMetricEvent implements MetricEvent {

  private final long timestamp;
  private final String type;
  private final int connectionId;

  public IncomingTypeMetricEvent(long timestamp, String type, int connectionId) {
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

  public void accept(MetricsServiceImpl.MetricEventVisitor visitor) {
    visitor.visit(this);
  }
}
