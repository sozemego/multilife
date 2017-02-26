package soze.multilife.server.metrics;

import soze.multilife.events.EventHandler;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.server.connection.outward.Connection;
import soze.multilife.server.connection.outward.ConnectionDecorator;
import soze.multilife.server.metrics.events.InstanceMetricEvent;

/**
 * A decorator for the connection, which allows for logging
 * based on type of the sent object.
 */
public class InstanceMetricsConnection extends ConnectionDecorator {

  private final EventHandler eventHandler;

  public InstanceMetricsConnection(Connection decorated, EventHandler eventHandler) {
	super(decorated);
	this.eventHandler = eventHandler;
  }

  @Override
  public long getId() {
	return super.getId();
  }

  @Override
  public void send(OutgoingMessage msg) {
	postEvent(msg);
	super.send(msg);
  }

  /**
   * Assemblers and posts event based on this message.
   *
   * @param msg outgoing message
   */
  private void postEvent(OutgoingMessage msg) {
	InstanceMetricEvent event = createEvent(msg);
	eventHandler.post(event);
  }

  /**
   * Creates {@link InstanceMetricEvent}.
   *
   * @param msg outgoing message
   * @return event constructed from the data
   */
  private InstanceMetricEvent createEvent(OutgoingMessage msg) {
	long timeStamp = System.nanoTime();
	return new InstanceMetricEvent(timeStamp, msg.getType().toString(), getId());
  }

}
