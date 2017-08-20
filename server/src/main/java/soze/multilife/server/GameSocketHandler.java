package soze.multilife.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.Player;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.connection.ConnectionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A basic delegate which handles web socket events and passes them along
 * to the simulation. Incoming messages are deserialized.
 */
@WebSocket
public class GameSocketHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GameSocketHandler.class);

	private final LoginService loginService;
	private final Lobby lobby;
	private final ConnectionFactory connectionFactory;
	private final AtomicInteger id = new AtomicInteger(0);
	private final Map<Session, Integer> sessionIdMap = new ConcurrentHashMap<>();
	private final ObjectMapper mapper = new ObjectMapper();

	public GameSocketHandler(Lobby lobby, LoginService loginService, ConnectionFactory connectionFactory) {
		this.lobby = lobby;
		this.loginService = loginService;
		this.connectionFactory = connectionFactory;
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
		LOG.trace("GOT MESSAGE STRING [{}]", msg);
	}

	@OnWebSocketFrame
	public void onBinaryMessage(Session session, Frame frame) {
		LOG.trace("GOT FRAME [{}]", frame);
		LOG.trace("PAYLOAD [{}]", frame.getPayload().array());
	}


	private void sendMessage(Session session, String msg) throws java.io.IOException {
		IncomingMessage inc = mapper.readValue(msg, IncomingMessage.class);
		if(inc.getType() == IncomingType.LOGIN) {
			Player player = loginService.login((LoginMessage)inc, getConnection(sessionIdMap.get(session), session));
			lobby.addPlayer(player);
			return;
		}
		lobby.onMessage(inc, sessionIdMap.get(session));
	}

	private Connection getConnection(int id, Session session) {
		return connectionFactory.getConnection(id, session);
	}

}
