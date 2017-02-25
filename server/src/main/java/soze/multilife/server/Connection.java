package soze.multilife.server;

import org.webbitserver.WebSocketConnection;

/**
 * A wrapper for connection class, which simplifies interactions
 * with WebSocketConnection objects.
 */
public class Connection {

	private final WebSocketConnection connection;

	public Connection(WebSocketConnection connection) {
		this.connection = connection;
	}

	public long getId() {
		return (long) connection.httpRequest().id();
	}

	public void send(String msg) {
		this.connection.send(msg);
	}

}
