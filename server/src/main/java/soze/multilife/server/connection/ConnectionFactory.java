package soze.multilife.server.connection;

import org.webbitserver.WebSocketConnection;
import soze.multilife.events.EventHandler;
import soze.multilife.server.metrics.MetricsConnection;

/**
 * Class used for wrapping socket objects provided
 * by servlet containers.
 */
public class ConnectionFactory {

  private final EventHandler eventHandler;

  public ConnectionFactory(EventHandler eventHandler) {
	this.eventHandler = eventHandler;
  }

  public Connection getBaseConnection(WebSocketConnection connection) {
	return new BaseConnection(connection);
  }

  public Connection getMetricsConnection(WebSocketConnection connection) {
	return new MetricsConnection(new BaseConnection(connection), eventHandler);
  }

}
