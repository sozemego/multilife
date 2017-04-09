package soze.multilife.server.connection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.messages.outgoing.OutgoingMessage;

import java.io.IOException;

/**
 * Base simulation facing {@link Connection} implementation.
 * Serializes the message and delegates the string further down.
 */
public class BaseConnection implements Connection {

  private static final Logger LOG = LoggerFactory.getLogger(BaseConnection.class);

  private final long id;
  private final Session session;
  private final ObjectMapper mapper = new ObjectMapper();

  public BaseConnection(long id, Session session) {
	this.id = id;
	this.session = session;
  }

  @Override
  public long getId() {
	return id;
  }

  @Override
  public void send(OutgoingMessage message) {
    try {
	  this.session.getRemote().sendString(serialize(message));
	} catch (IOException e) {
		LOG.warn("Base connection could not send string.", e);
	}
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
