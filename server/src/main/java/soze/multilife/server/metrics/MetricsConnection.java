package soze.multilife.server.metrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.webbitserver.WebSocketConnection;
import soze.multilife.events.EventHandler;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.server.connection.BaseConnection;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.metrics.events.InstanceMetricEvent;
import soze.multilife.server.metrics.events.SerializedMetricEvent;

/**
 * A {@link Connection} implementation which measures various
 * metrics about outgoing messages.
 */
public class MetricsConnection extends BaseConnection {

  private final EventHandler eventHandler;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public MetricsConnection(WebSocketConnection connection, EventHandler eventHandler) {
	super(connection);
	this.eventHandler = eventHandler;
  }

  @Override
  public long getId() {
	return super.getId();
  }

  @Override
  public void send(OutgoingMessage message) {
	postEvent(message);
	String serializedMessage = serialize(message);
	postEvent(serializedMessage);
	super.send(message);
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

  /**
   * Assembles and posts an event based on this data.
   *
   * @param message
   */
  private void postEvent(String message) {
	SerializedMetricEvent event = createEvent(message);
	eventHandler.post(event);
  }

  /**
   * Creates an event based on this message.
   *
   * @param message
   * @return
   */
  private SerializedMetricEvent createEvent(String message) {
	long timeStamp = System.nanoTime();
	return new SerializedMetricEvent(timeStamp, getId(), message.length() / 2);
  }

  private String serialize(OutgoingMessage message) {
	try {
	  return objectMapper.writeValueAsString(message);
	} catch (JsonProcessingException e) {
	  e.printStackTrace();
	  return "";
	}
  }


}
