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

  /**
   * Returns a default connection wrappers for webbit specific implementation.
   *
   * @param connection
   * @return
   */
  public Connection getConnection(WebSocketConnection connection) {
	return getMetricsConnection(connection);
  }

  private Connection getMetricsConnection(WebSocketConnection connection) {
	return new MetricsConnection(connection, eventHandler);
  }

}
