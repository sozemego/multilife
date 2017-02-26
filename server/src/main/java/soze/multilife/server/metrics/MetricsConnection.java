package soze.multilife.server.metrics;

import soze.multilife.events.EventHandler;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.connection.ConnectionDecorator;

/**
 * A decorator for the connection, which allows logging
 * of sent (outbound) data.
 */
public class MetricsConnection extends ConnectionDecorator {

  private final EventHandler eventHandler;

  public MetricsConnection(Connection decorated, EventHandler eventHandler) {
	super(decorated);
	this.eventHandler = eventHandler;
  }

  @Override
  public long getId() {
	return super.getId();
  }

  @Override
  public void send(String msg) {
    postEvent(msg);
	super.send(msg);
  }

  /**
   * Assemblers and posts event based on this message.
   * @param msg outgoing message (most likely serialized json)
   */
  private void postEvent(String msg) {
	MetricEvent event = createEvent(msg);
	eventHandler.post(event);
  }

  /**
   * Creates {@link MetricEvent}
   * @param msg outgoing message (most likely serialized json)
   * @return event constructed from the data
   */
  private MetricEvent createEvent(String msg) {
	long timeStamp = System.nanoTime();
	return new MetricEvent(timeStamp, getConnection().getId(), msg.length() * 2);
  }



}
