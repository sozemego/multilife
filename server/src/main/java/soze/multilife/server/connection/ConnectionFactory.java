package soze.multilife.server.connection;

import org.webbitserver.WebSocketConnection;

/**
 * Class used for wrapping socket objects provided
 * by servlet containers.
 */
public class ConnectionFactory {

	public Connection getBaseConnection(WebSocketConnection connection) {
	  return new BaseConnection(connection);
	}

	public Connection getLoggingConnection(WebSocketConnection connection) {
	  return new LoggingConnection(new BaseConnection(connection));
	}

}
