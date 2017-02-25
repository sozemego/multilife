package soze.multilife.server.connection;

import org.webbitserver.WebSocketConnection;

/**
 * A base connection which wraps around WebSocketConnection.
 */
public class BaseConnection implements Connection {

  private final WebSocketConnection connection;

  public BaseConnection(WebSocketConnection connection) {
	this.connection = connection;
  }

  public long getId() {
	return (long) connection.httpRequest().id();
  }

  public void send(String msg) {
	this.connection.send(msg);
  }

}
