package soze.multilife.server.connection;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.messages.outgoing.*;
import soze.multilife.utils.JsonUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Base simulation facing {@link Connection} implementation.
 * Serializes the message and delegates the string further down.
 */
public class BaseConnection implements Connection {

	private static final Logger LOG = LoggerFactory.getLogger(BaseConnection.class);

	private final int id;
	private final Session session;

	public BaseConnection(int id, Session session) {
		this.id = id;
		this.session = Objects.requireNonNull(session);
	}

	@Override
	public int getId() {
		return id;
	}

//	@Override
//	public void send(OutgoingMessage message) {
//		try {
//			String json = JsonUtils.stringify(message);
//			this.session.getRemote().sendStringByFuture(json);
//		} catch (IOException e) {
//			LOG.warn("Base connection could not send string.", e);
//		}
//	}

	@Override
	public void send(OutgoingMessage message) {
		LOG.trace("Sending [{}]", message);
		Objects.requireNonNull(message);
		if(message instanceof MetricsMessage) {
			try {
				String json = JsonUtils.stringify(message);
				this.session.getRemote().sendStringByFuture(json);
			} catch (IOException e) {
				LOG.warn("Base connection could not send string.", e);
			}
			return;
		}
		OutgoingMessageConverterVisitor converter = new OutgoingMessageConverterVisitor();
		message.accept(converter);
		final byte[] payload = converter.getPayload();
		this.send(payload);
	}

	@Override
	public void send(byte[] bytes) {
		try {
			this.session.getRemote().sendBytesByFuture(ByteBuffer.wrap(bytes));
		} catch (Exception e) {
			LOG.warn("Base connection could not send bytes. ", e);
		}
	}

	@Override
	public void disconnect() {
		session.close();
	}

	@Override
	public String toString() {
		return "BaseConnection{" +
				"id=" + id +
				'}';
	}
}
