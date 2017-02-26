package soze.multilife.server.connection.outward;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.server.connection.inward.SerializedObjectConnection;

/**
 * Base simulation facing {@link Connection} implementation.
 * Serializes the message and delegates the string further down.
 */
public class BaseConnection implements Connection {

  private final ObjectMapper mapper = new ObjectMapper();
  private final SerializedObjectConnection connection;

  public BaseConnection(SerializedObjectConnection connection) {
	this.connection = connection;
  }

  @Override
  public long getId() {
	return connection.getId();
  }

  @Override
  public void send(OutgoingMessage message) {
	this.connection.send(serialize(message));
  }

  private String serialize(OutgoingMessage message) {
    try {
      return mapper.writeValueAsString(message);
	} catch (JsonProcessingException e) {
      e.printStackTrace();
      return "";
	}
  }

}
