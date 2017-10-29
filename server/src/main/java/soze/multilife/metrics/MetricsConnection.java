package soze.multilife.metrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import soze.multilife.events.EventBus;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.messages.outgoing.OutgoingType;
import soze.multilife.metrics.events.OutgoingSizeMetricEvent;
import soze.multilife.metrics.events.OutgoingTypeMetricEvent;
import soze.multilife.server.connection.BaseConnection;
import soze.multilife.server.connection.Connection;

import java.util.Objects;

/**
 * A {@link Connection} implementation which measures various
 * metrics about outgoing messages.
 */
public class MetricsConnection extends BaseConnection {

  private final EventBus eventBus;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public MetricsConnection(int id, Session session, EventBus eventBus) {
    super(id, session);
    this.eventBus = Objects.requireNonNull(eventBus);
  }

  public void send(OutgoingMessage message) {
    postEvent(message);
    String serializedMessage = serialize(message);
    postEvent(serializedMessage);
    super.send(message);
  }

  public void send(byte[] bytes) {
    if (bytes.length == 0) {
      throw new IllegalArgumentException("You cannot send empty messages.");
    }

    OutgoingType type = OutgoingType.getType(bytes[0]);
    postEvent(type);
    postEvent(bytes);
    super.send(bytes);
  }

  private void postEvent(byte[] bytes) {
    long timeStamp = System.nanoTime();
    eventBus.post(new OutgoingSizeMetricEvent(timeStamp, getId(), bytes.length));
  }

  private void postEvent(OutgoingType type) {
    long timeStamp = System.nanoTime();
    eventBus.post(new OutgoingTypeMetricEvent(timeStamp, type.toString(), getId()));
  }

  /**
   * Assemblers and posts event based on this message.
   *
   * @param msg outgoing message
   */
  private void postEvent(OutgoingMessage msg) {
    OutgoingTypeMetricEvent event = createEvent(msg);
    eventBus.post(event);
  }

  /**
   * Creates {@link OutgoingTypeMetricEvent}.
   *
   * @param msg outgoing message
   * @return event constructed from the data
   */
  private OutgoingTypeMetricEvent createEvent(OutgoingMessage msg) {
    long timeStamp = System.nanoTime();
    return new OutgoingTypeMetricEvent(timeStamp, msg.getType().toString(), getId());
  }

  /**
   * Assembles and posts an event based on this data.
   */
  private void postEvent(String message) {
    OutgoingSizeMetricEvent event = createEvent(message);
    eventBus.post(event);
  }

  /**
   * Creates an event based on this message.
   */
  private OutgoingSizeMetricEvent createEvent(String message) {
    long timeStamp = System.nanoTime();
    return new OutgoingSizeMetricEvent(timeStamp, getId(), message.getBytes().length);
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
