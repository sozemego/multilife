package soze.multilife.server.connection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.webbitserver.WebSocketConnection;
import soze.multilife.messages.outgoing.OutgoingMessage;

/**
 * Base simulation facing {@link Connection} implementation.
 * Serializes the message and delegates the string further down.
 */
public class BaseConnection implements Connection {

  private final ObjectMapper mapper = new ObjectMapper();
  private final WebSocketConnection connection;

  public BaseConnection(WebSocketConnection connection) {
	this.connection = connection;
  }

  @Override
  public long getId() {
	return (long) connection.httpRequest().id();
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
