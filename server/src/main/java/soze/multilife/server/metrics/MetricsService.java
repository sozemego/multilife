package soze.multilife.server.metrics;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.server.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.server.metrics.events.PlayerLoggedEvent;
import soze.multilife.server.metrics.events.SerializedMetricEvent;
import soze.multilife.server.metrics.events.TypeMetricEvent;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * A service for storing, calculating and reporting various metrics.
 */
public class MetricsService implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MetricsService.class);

	private final Supplier<Long> calculateMetricsInterval;

	private final Queue<Object> events = new ConcurrentLinkedQueue<>();

	private long totalBytesSent = 0;
	private double averageBytesSent = 0d;
	private long totalMessagesSent = 0;

	private long lastKilobytePerSecondCalculationTime = System.currentTimeMillis();
	private double totalBytesDuringLastCheck = 0;
	private double currentKilobytesPerSecond = 0d;
	private long lastSaveTime = 0L;
	private long intervalBetweenSaves = 1000 * 60 * 60;

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
				if (event instanceof TypeMetricEvent) {
					process((TypeMetricEvent) event);
				}
			}

			calculateKilobytesPerSecond();

			long currentTime = System.currentTimeMillis();
			if(currentTime > lastSaveTime + intervalBetweenSaves) {
				LOG.info("Saving last hours data.");
				lastSaveTime = currentTime;
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

	private void process(TypeMetricEvent event) {
		String type = event.getType();
		//synchronized (typeCountMap) {
		Long count = typeCountMap.get(type);
		typeCountMap.put(type, count == null ? 1 : ++count);
		//}
	}

	private void calculateKilobytesPerSecond() {
		long timePassed = System.currentTimeMillis() - lastKilobytePerSecondCalculationTime;
		double kilobytesSentSinceLastCheck = (totalBytesSent - totalBytesDuringLastCheck) / 1024;
		currentKilobytesPerSecond = kilobytesSentSinceLastCheck / (timePassed / 1000);
		totalBytesDuringLastCheck = totalBytesSent;
		LOG.info("Currently sending [{}] kb/s", currentKilobytesPerSecond);
	}

	@Subscribe
	public void handleInstanceMetricEvent(TypeMetricEvent event) {
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
