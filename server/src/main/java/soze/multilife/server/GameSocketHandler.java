package soze.multilife.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebSocketConnection;
import soze.multilife.messages.incoming.IncomingMessage;

/**
 * A basic delegate which handles web socket events and passes them along
 * to the simulation. Incoming messages are deserialized.
 */
public class GameSocketHandler extends BaseWebSocketHandler {

    private final Lobby lobby;
    private final ObjectMapper mapper = new ObjectMapper();

    public GameSocketHandler(Lobby lobby) {
        this.lobby = lobby;
    }

		@Override
		public void onOpen(WebSocketConnection connection) throws Exception {
			System.out.println("User connected. ConnectionID: " + connection.httpRequest().id());
			lobby.onConnect(getConnection(connection));
		}

		@Override
		public void onClose(WebSocketConnection connection) throws Exception {
			System.out.println("User disconnected. ConnectionID: " + connection.httpRequest().id());
			lobby.onDisconnect(getConnection(connection));
		}

		@Override
		public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
			IncomingMessage inc = mapper.readValue(msg, IncomingMessage.class);
			lobby.onMessage(inc, (long) connection.httpRequest().id());
		}

		private Connection getConnection(WebSocketConnection connection) {
    	return new Connection((long) connection.httpRequest().id(), connection);
		}

}
