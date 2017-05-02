package soze.multilife.metrics;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.configuration.MetricsConfigurationImpl;
import soze.multilife.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.metrics.events.PlayerLoggedEvent;
import soze.multilife.metrics.events.SerializedMetricEvent;
import soze.multilife.metrics.events.TypeMetricEvent;
import soze.multilife.metrics.repository.MetricsRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A service for storing, calculating and reporting various metrics.
 */
public class MetricsService implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MetricsService.class);

	private final MetricsRepository repository;
	private final MetricsConfigurationImpl configuration;

	private final Queue<Object> events = new ConcurrentLinkedQueue<>();

	private long totalBytesSent = 0;
	private double averageBytesSent = 0d;
	private long totalMessagesSent = 0;

	private long lastKbsCalculationTime = System.currentTimeMillis();
	private double totalBytesDuringLastCheck = 0;
	private double averageKbs = 0d;
	private long lastSaveTime = 0L;
	private int maxPlayersBeforeSave = 0;

	private final Map<String, Long> typeCountMap = new ConcurrentHashMap<>();
	private final Map<Long, Long> playerMap = new ConcurrentHashMap<>();

	public MetricsService(MetricsRepository repository, MetricsConfigurationImpl configuration) {
		this.repository = repository;
		this.configuration = configuration;
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

			countMaxPlayers();
			calculateKilobytesPerSecond();
			save();

			try {
				Thread.sleep(configuration.getCalculateMetricsInterval());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void process(PlayerLoggedEvent playerEvent) {
		long playerId = playerEvent.getPlayerId();
		long instanceId = playerEvent.getInstanceId();
		playerMap.put(playerId, instanceId);
	}

	private void process(PlayerDisconnectedEvent playerEvent) {
		playerMap.remove(playerEvent.getPlayerId());
	}

	private void process(SerializedMetricEvent event) {
		totalBytesSent += event.getBytesSent();
		totalMessagesSent++;
		averageBytesSent = totalBytesSent / totalMessagesSent;
	}

	private void process(TypeMetricEvent event) {
		String type = event.getType();
		Long count = typeCountMap.get(type);
		typeCountMap.put(type, count == null ? 1 : ++count);
	}

	private void calculateKilobytesPerSecond() {
		long currentTime = System.currentTimeMillis();
		long timePassedMs = currentTime - lastKbsCalculationTime;
		double kilobytesSentSinceLastCheck = (totalBytesSent - totalBytesDuringLastCheck) / 1024;
		averageKbs = kilobytesSentSinceLastCheck / (timePassedMs / 1000);
		totalBytesDuringLastCheck = totalBytesSent;
		lastKbsCalculationTime = currentTime;
		LOG.trace("Currently sending [{}] kb/s", averageKbs);
	}

	private void countMaxPlayers() {
		int playerCount = playerMap.size();
		if(playerCount > maxPlayersBeforeSave) {
			maxPlayersBeforeSave = playerCount;
		}
		LOG.trace("Currently [{}] players.", playerCount);
	}

	private void saveKbs() {
		repository.saveKilobytesPerSecond(averageKbs, Instant.now().toEpochMilli());
	}

	private void saveMaxPlayers() {
		repository.saveMaxPlayers(maxPlayersBeforeSave, Instant.now().toEpochMilli());
		maxPlayersBeforeSave = 0;
	}

	private void save() {
		long currentTime = System.currentTimeMillis();
		if(currentTime > lastSaveTime + configuration.metricsSaveInterval()) {
			saveKbs();
			saveMaxPlayers();
			lastSaveTime = currentTime;
		}
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

	public double getAverageKbs() {
		return averageKbs;
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
