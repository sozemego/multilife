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
		Objects.requireNonNull(message);
		if(message instanceof PongMessage) {
			this.send(MessageConverter.convertMessage((PongMessage) message));
		} else if (message instanceof CellList) {
			this.send(MessageConverter.convertMessage((CellList) message));
		} else if (message instanceof TickData) {
			this.send(MessageConverter.convertMessage((TickData) message));
		} else if (message instanceof MapData) {
			this.send(MessageConverter.convertMessage((MapData) message));
		} else if (message instanceof PlayerIdentity) {
			this.send(MessageConverter.convertMessage((PlayerIdentity) message));
		} else if (message instanceof TimeRemainingMessage) {
			this.send(MessageConverter.convertMessage((TimeRemainingMessage) message));
		} else {
			try {
				String json = JsonUtils.stringify(message);
				this.session.getRemote().sendStringByFuture(json);
			} catch (IOException e) {
				LOG.warn("Base connection could not send string.", e);
			}
		}
	}

	@Override
	public void send(byte[] bytes) {
		this.session.getRemote().sendBytesByFuture(ByteBuffer.wrap(bytes));
	}

	@Override
	public void disconnect() {
		session.close();
	}

}
