package soze.multilife.server;

import org.webbitserver.WebSocketConnection;

/**
 * A wrapper for connection class, which simplifies interactions
 * with WebSocketConnection objects.
 */
public class Connection {

	private final long id;
	private final WebSocketConnection connection;

	public Connection(long id, WebSocketConnection connection) {
		this.id = id;
		this.connection = connection;
	}

	public long getId() {
		return id;
	}

	public void send(String msg) {
		this.connection.send(msg);
	}

}
