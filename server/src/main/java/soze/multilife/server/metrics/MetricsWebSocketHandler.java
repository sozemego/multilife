package soze.multilife.server.metrics;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.messages.outgoing.MetricsMessage;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.connection.ConnectionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A metrics endpoint handler for live data.
 */
@WebSocket
public class MetricsWebSocketHandler implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MetricsWebSocketHandler.class);

	private final long metricsPushUpdateRate;

	private final MetricsService metricsService;
	private final ConnectionFactory connectionFactory;

	private final AtomicLong nextId = new AtomicLong();
	private final Map<Session, Long> sessionIdMap = new ConcurrentHashMap<>();
	private final Map<Long, Connection> connections = new ConcurrentHashMap<>();

	public MetricsWebSocketHandler(long metricsPushUpdateRate, MetricsService metricsService, ConnectionFactory connectionFactory) {
		this.metricsPushUpdateRate = metricsPushUpdateRate;
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
				Thread.sleep(metricsPushUpdateRate);
			} catch (InterruptedException e) {
				LOG.error("Thread interrupted [{}] ", e);
			}

		}
	}

	private MetricsMessage createMetricsMessage() {
		long totalBytesSent = metricsService.getTotalBytesSent();
		long messagesSent = metricsService.getTotalMessagesSent();
		double averageBytesSent = metricsService.getAverageBytesSent();
		Map<String, Long> typeCountMap = metricsService.getTypeCountMap();
		Map<Long, Long> playerMap = metricsService.getPlayerMap();

		return new MetricsMessage(
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
