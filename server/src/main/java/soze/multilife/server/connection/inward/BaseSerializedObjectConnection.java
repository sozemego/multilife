package soze.multilife.server.connection.inward;

import org.webbitserver.WebSocketConnection;

/**
 * Created by soze on 2/26/2017.
 */
public class BaseSerializedObjectConnection implements SerializedObjectConnection {

  private final WebSocketConnection connection;

  public BaseSerializedObjectConnection(WebSocketConnection connection) {
	this.connection = connection;
  }

  @Override
  public long getId() {
	return (long) connection.httpRequest().id();
  }

  @Override
  public void send(String message) {
	connection.send(message);
  }
}
