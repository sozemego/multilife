package soze.multilife.metrics;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.configuration.interfaces.MetricsConfiguration;
import soze.multilife.messages.outgoing.MetricsMessage;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.connection.ConnectionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A metrics endpoint handler for live data.
 */
@WebSocket
public class MetricsWebSocketHandlerImpl implements MetricsWebSocketHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MetricsWebSocketHandlerImpl.class);

	private final MetricsConfiguration configuration;

	private final MetricsService metricsService;
	private final ConnectionFactory connectionFactory;

	private final AtomicInteger nextId = new AtomicInteger();
	private final Map<Session, Integer> sessionIdMap = new ConcurrentHashMap<>();
	private final Map<Integer, Connection> connections = new ConcurrentHashMap<>();

	public MetricsWebSocketHandlerImpl(MetricsConfiguration configuration, MetricsService metricsService, ConnectionFactory connectionFactory) {
		this.configuration = configuration;
		this.metricsService = metricsService;
		this.connectionFactory = connectionFactory;
	}

	@OnWebSocketConnect
	public void onOpen(Session session) throws Exception {
		Connection conn = getConnection(session);
		sessionIdMap.put(session, conn.getId());
		synchronized (connections) {
			connections.put(conn.getId(), conn);
		}
	}

	@OnWebSocketClose
	public void onClose(Session session, int statusCode, String reason) throws Exception {
		long id = sessionIdMap.remove(session);
		synchronized (connections) {
			connections.remove(id);
		}
	}

	@OnWebSocketMessage
	public void onMessage(Session session, String msg) throws Exception {

	}

	public void run() {
		while (true) {

			synchronized (connections) {

				if (connections.size() > 0) {

					MetricsMessage message = createMetricsMessage();
					for (Connection connection : connections.values()) {
						connection.send(message);
					}
				}
			}

			try {
				Thread.sleep(configuration.getMetricsPushInterval());
			} catch (InterruptedException e) {
				LOG.error("Thread interrupted [{}] ", e);
			}

		}
	}

	private MetricsMessage createMetricsMessage() {
		double averageKbs = metricsService.getAverageKbs();
		long totalBytesSent = metricsService.getTotalBytesSent();
		long messagesSent = metricsService.getTotalMessagesSent();
		double averageBytesSent = metricsService.getAverageBytesSent();
		Map<String, Long> typeCountMap = metricsService.getTypeCountMap();
		Map<Long, Long> playerMap = metricsService.getPlayerMap();

		return new MetricsMessage(
			averageKbs,
			totalBytesSent,
			averageBytesSent,
			messagesSent,
			typeCountMap,
			playerMap
		);
	}

	private Connection getConnection(Session session) {
		return connectionFactory.getConnection(nextId.incrementAndGet(), session);
	}

}
