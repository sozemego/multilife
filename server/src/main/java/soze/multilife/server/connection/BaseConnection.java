package soze.multilife.server.connection;

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
			String json = JsonUtils.stringify(message);
			this.session.getRemote().sendStringByFuture(json);
		} catch (IOException e) {
			LOG.warn("Base connection could not send string.", e);
		}
	}

	@Override
	public void disconnect() {
		session.close();
	}

}
