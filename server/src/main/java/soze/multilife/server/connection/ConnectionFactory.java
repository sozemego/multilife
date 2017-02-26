package soze.multilife.server.connection;

import org.webbitserver.WebSocketConnection;
import soze.multilife.events.EventHandler;
import soze.multilife.server.connection.inward.BaseSerializedObjectConnection;
import soze.multilife.server.connection.inward.SerializedObjectConnection;
import soze.multilife.server.connection.outward.BaseConnection;
import soze.multilife.server.connection.outward.Connection;
import soze.multilife.server.metrics.InstanceMetricsConnection;
import soze.multilife.server.metrics.SerializedObjectMetricsConnection;

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
   * @param connection
   * @return
   */
  public Connection getConnection(WebSocketConnection connection) {
    return getMetricsConnection(connection);
  }

  private Connection getMetricsConnection(WebSocketConnection connection) {
	return new InstanceMetricsConnection(new BaseConnection(getSerializedObjectConnection(connection)), eventHandler);
  }

  private SerializedObjectConnection getSerializedObjectConnection(WebSocketConnection connection) {
    return new SerializedObjectMetricsConnection(new BaseSerializedObjectConnection(connection), eventHandler);
  }

}
