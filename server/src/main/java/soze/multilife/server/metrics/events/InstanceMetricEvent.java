package soze.multilife.server.metrics.events;

/**
 * Event constructed based on the instance of outgoing data.
 *
 * @see soze.multilife.messages.outgoing.OutgoingMessage
 */
public class InstanceMetricEvent {

  private final long timeStamp;
  private final String type;
  private final long connectionId;

  public InstanceMetricEvent(long timeStamp, String type, long connectionId) {
	this.timeStamp = timeStamp;
	this.type = type;
	this.connectionId = connectionId;
  }

  public long getTimeStamp() {
	return timeStamp;
  }

  public String getType() {
	return type;
  }

  public long getConnectionId() {
	return connectionId;
  }
}
