package soze.multilife.server.metrics;

import soze.multilife.events.EventHandler;
import soze.multilife.server.connection.inward.SerializedObjectConnection;
import soze.multilife.server.connection.inward.SerializedObjectConnectionDecorator;
import soze.multilife.server.metrics.events.SerializedMetricEvent;

/**
 * Created by soze on 2/26/2017.
 */
public class SerializedObjectMetricsConnection extends SerializedObjectConnectionDecorator {

  private final EventHandler eventHandler;

  public SerializedObjectMetricsConnection(SerializedObjectConnection serializedObjectConnection, EventHandler eventHandler) {
	super(serializedObjectConnection);
	this.eventHandler = eventHandler;
  }

  @Override
  public long getId() {
	return super.getId();
  }

  @Override
  public void send(String message) {
    postEvent(message);
	super.send(message);
  }

  /**
   * Assemblers and posts event based on this message.
   *
   * @param msg outgoing message (most likely serialized json)
   */
  private void postEvent(String msg) {
	SerializedMetricEvent event = createEvent(msg);
	eventHandler.post(event);
  }

  /**
   * Creates {@link SerializedMetricEvent}.
   *
   * @param msg outgoing message (most likely serialized json)
   * @return event constructed from the data
   */
  private SerializedMetricEvent createEvent(String msg) {
	long timeStamp = System.nanoTime();
	return new SerializedMetricEvent(timeStamp, getId(), msg.length() / 2);
  }

}
