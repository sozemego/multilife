package soze.multilife.server.connection;

import org.eclipse.jetty.websocket.api.Session;
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
   * Returns a default connection wrapper.
   * @return
   */
  public Connection getConnection(long id, Session session) {
	return getMetricsConnection(id, session);
  }

  private Connection getMetricsConnection(long id, Session session) {
	return new MetricsConnection(id, session, eventHandler);
  }

}
