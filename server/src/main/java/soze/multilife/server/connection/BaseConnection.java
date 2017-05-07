package soze.multilife.server.connection;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.utils.JsonUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * Base simulation facing {@link Connection} implementation.
 * Serializes the message and delegates the string further down.
 */
public class BaseConnection implements Connection {

	private static final Logger LOG = LoggerFactory.getLogger(BaseConnection.class);

	private final long id;
	private final Session session;

	public BaseConnection(long id, Session session) {
		this.id = id;
		this.session = Objects.requireNonNull(session);
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void send(OutgoingMessage message) {
		Objects.requireNonNull(message);
		try {
			this.session.getRemote().sendString(stringify(message));
		} catch (IOException e) {
			LOG.warn("Base connection could not send string.", e);
		}
	}

	@Override
	public void disconnect() {
		session.close();
	}

	private String stringify(OutgoingMessage message) {
		try {
			return JsonUtils.stringify(message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

}
