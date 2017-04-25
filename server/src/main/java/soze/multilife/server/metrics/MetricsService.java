package soze.multilife.server.metrics;

import com.google.common.eventbus.Subscribe;
import soze.multilife.server.metrics.events.InstanceMetricEvent;
import soze.multilife.server.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.server.metrics.events.PlayerLoggedEvent;
import soze.multilife.server.metrics.events.SerializedMetricEvent;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * A service for storing, calculating and reporting various metrics.
 */
public class MetricsService implements Runnable {

	private final Supplier<Long> calculateMetricsInterval;

	private final Queue<Object> events = new ConcurrentLinkedQueue<>();

	private long totalBytesSent = 0;
	private double averageBytesSent = 0d;
	private long totalMessagesSent = 0;

	private final Map<String, Long> typeCountMap = new ConcurrentHashMap<>();
	private final Map<Long, Long> playerMap = new ConcurrentHashMap<>();

	public MetricsService(Supplier<Long> calculateMetricsInterval) {
		this.calculateMetricsInterval = calculateMetricsInterval;
	}

	@Override
	public void run() {
		while (true) {

			Object event;
			while ((event = events.poll()) != null) {
				if (event instanceof PlayerLoggedEvent) {
					process((PlayerLoggedEvent) event);
				}
				if (event instanceof PlayerDisconnectedEvent) {
					process((PlayerDisconnectedEvent) event);
				}
				if (event instanceof SerializedMetricEvent) {
					process((SerializedMetricEvent) event);
				}
				if (event instanceof InstanceMetricEvent) {
					process((InstanceMetricEvent) event);
				}
			}

			try {
				Thread.sleep(calculateMetricsInterval.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void process(PlayerLoggedEvent playerEvent) {
		//synchronized (playerMap) {
		long playerId = playerEvent.getPlayerId();
		long instanceId = playerEvent.getInstanceId();
		playerMap.put(playerId, instanceId);
		//}
	}

	private void process(PlayerDisconnectedEvent playerEvent) {
		//synchronized (playerMap) {
		playerMap.remove(playerEvent.getPlayerId());
		//}
	}

	private void process(SerializedMetricEvent event) {
		totalBytesSent += event.getBytesSent();
		totalMessagesSent++;
		averageBytesSent = totalBytesSent / totalMessagesSent;
	}

	private void process(InstanceMetricEvent event) {
		String type = event.getType();
		//synchronized (typeCountMap) {
		Long count = typeCountMap.get(type);
		typeCountMap.put(type, count == null ? 1 : ++count);
		//}
	}

	@Subscribe
	public void handleInstanceMetricEvent(InstanceMetricEvent event) {
		events.add(event);
	}

	@Subscribe
	public void handleSerializedMetricEvent(SerializedMetricEvent event) {
		events.add(event);
	}

	@Subscribe
	public void handlePlayerLoggedEvent(PlayerLoggedEvent event) {
		events.add(event);
	}

	@Subscribe
	public void handlePlayerDisconnectedEvent(PlayerDisconnectedEvent event) {
		events.add(event);
	}

	public long getTotalBytesSent() {
		return totalBytesSent;
	}

	public double getAverageBytesSent() {
		return averageBytesSent;
	}

	public long getTotalMessagesSent() {
		return totalMessagesSent;
	}

	public Map<String, Long> getTypeCountMap() {
		return typeCountMap;
	}

	public Map<Long, Long> getPlayerMap() {
		return playerMap;
	}
}
