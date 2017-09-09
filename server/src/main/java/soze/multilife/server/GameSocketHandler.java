package soze.multilife.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.events.EventBus;
import soze.multilife.game.Player;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingMessageConverter;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.metrics.events.IncomingSizeMetricEvent;
import soze.multilife.metrics.events.IncomingTypeMetricEvent;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.connection.ConnectionFactory;
import spark.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A basic delegate which handles web socket events and passes them along
 * to the game. Incoming messages are deserialized.
 */
@WebSocket
public class GameSocketHandler { //TODO decorate this for metric events?

	private static final Logger LOG = LoggerFactory.getLogger(GameSocketHandler.class);

	private final LoginService loginService;
	private final Lobby lobby;
	private final ConnectionFactory connectionFactory;
	private final EventBus eventBus;

	private final AtomicInteger id = new AtomicInteger(0);
	private final Map<Session, Integer> sessionIdMap = new ConcurrentHashMap<>();
	private final ObjectMapper mapper = new ObjectMapper();

	public GameSocketHandler(
			Lobby lobby,
			LoginService loginService,
			ConnectionFactory connectionFactory,
			EventBus eventBus
	) {
		this.lobby = Objects.requireNonNull(lobby);
		this.loginService = Objects.requireNonNull(loginService);
		this.connectionFactory = Objects.requireNonNull(connectionFactory);
		this.eventBus = Objects.requireNonNull(eventBus);
	}

	@OnWebSocketConnect
	public void onOpen(Session session) throws Exception {
		connect(session);
	}

	private void connect(Session session) {
		int nextId = id.incrementAndGet();
		LOG.info("User connected. ConnectionID [{}].", nextId);
		sessionIdMap.put(session, nextId);
		lobby.onConnect(getConnection(nextId, session));
	}

	@OnWebSocketClose
	public void onClose(Session session, int statusCode, String reason) throws Exception {
		disconnect(session, statusCode, reason);
	}

	private void disconnect(Session session, int statusCode, String reason) {
		int userId = sessionIdMap.remove(session);
		LOG.info("User disconnected. ConnectionID [{}]. Status code [{}]. Reason [{}]", userId, statusCode, reason);
		lobby.onDisconnect(getConnection(userId, session));
	}

	@OnWebSocketMessage
	public void onMessage(Session session, String msg) throws Exception {
		sendMessage(session, msg);
	}

	@OnWebSocketMessage
	public void onBinaryMessage(Session session, InputStream stream) {
		byte[] payload = toByteArray(stream);
		IncomingMessageConverter.convert(payload).ifPresent(message -> {
			sendMetricEvent(payload, sessionIdMap.get(session));
			sendMetricEvent(message, sessionIdMap.get(session));
			sendMessage(session, message);
		});
	}

	private byte[] toByteArray(InputStream stream) {
		try {
			return IOUtils.toByteArray(stream);
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[] {-1};
		}
	}

	private void sendMessage(Session session, String msg) throws java.io.IOException {
		IncomingMessage inc = mapper.readValue(msg, IncomingMessage.class);
		sendMessage(session, inc);
		sendMetricEvent(inc, sessionIdMap.get(session));
	}

	private void sendMessage(Session session, IncomingMessage incomingMessage) {
		if(incomingMessage.getType() == IncomingType.LOGIN) {
			Player player = loginService.login((LoginMessage)incomingMessage, getConnection(sessionIdMap.get(session), session));
			lobby.addPlayer(player);
			return;
		}
		lobby.onMessage(incomingMessage, sessionIdMap.get(session));
	}

	private Connection getConnection(int id, Session session) {
		return connectionFactory.getConnection(id, session);
	}

	private void sendMetricEvent(IncomingMessage incomingMessage, int connectionId) {
		long timestamp = System.nanoTime();
		IncomingTypeMetricEvent event = new IncomingTypeMetricEvent(timestamp, incomingMessage.getType().toString(), connectionId);
		eventBus.post(event);
	}

	private void sendMetricEvent(byte[] payload, int connectionId) {
		long timestamp = System.nanoTime();
		IncomingSizeMetricEvent event = new IncomingSizeMetricEvent(timestamp, connectionId, payload.length);
		eventBus.post(event);
	}

}
