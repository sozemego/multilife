package soze.multilife.server.connection;

import org.eclipse.jetty.websocket.api.Session;
import soze.multilife.events.EventBus;
import soze.multilife.metrics.MetricsConnection;

/**
 * Class used for wrapping socket objects provided
 * by servlet containers.
 */
public class ConnectionFactory {

	private final EventBus eventBus;

	public ConnectionFactory(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * Returns a default connection wrapper.
	 *
	 * @return
	 */
	public Connection getConnection(int id, Session session) {
		return getMetricsConnection(id, session);
	}

	private Connection getMetricsConnection(int id, Session session) {
		return new MetricsConnection(id, session, eventBus);
	}

}
