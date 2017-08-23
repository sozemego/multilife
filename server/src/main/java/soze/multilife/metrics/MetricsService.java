package soze.multilife.metrics;

import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.configuration.interfaces.MetricsConfiguration;
import soze.multilife.metrics.events.*;
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
	private final MetricsConfiguration configuration;

	private final Queue<Object> events = new ConcurrentLinkedQueue<>();

	private long totalBytesSent = 0;
	private double averageBytesSent = 0d;
	private long totalMessagesSent = 0;

	private long totalBytesReceived = 0;
	private double averageBytesReceived = 0d;
	private long totalMessagesReceived = 0;

	private long lastOutgoingKbsCalculationTime = System.currentTimeMillis();
	private double totalOutgoingBytesDuringLastCheck = 0;
	private double averageOutgoingKbs = 0d;

	private long lastIncomingKbsCalculationTime = System.currentTimeMillis();
	private double totalIncomingBytesDuringLastCheck = 0;
	private double averageIncomingKbs = 0d;

	private long lastSaveTime = 0L;
	private int maxPlayersBeforeSave = 0;

	private final Map<String, Long> outgoingTypeCountMap = new ConcurrentHashMap<>();
	private final Map<String, Long> incomingTypeCountMap = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> playerMap = new ConcurrentHashMap<>();

	public MetricsService(MetricsRepository repository, MetricsConfiguration configuration) {
		this.repository = repository;
		this.configuration = configuration;
	}

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
				if (event instanceof OutgoingSizeMetricEvent) {
					process((OutgoingSizeMetricEvent) event);
				}
				if (event instanceof OutgoingTypeMetricEvent) {
					process((OutgoingTypeMetricEvent) event);
				}
				if (event instanceof IncomingTypeMetricEvent) {
					process((IncomingTypeMetricEvent) event);
				}
				if (event instanceof IncomingSizeMetricEvent) {
					process((IncomingSizeMetricEvent) event);
				}
			}

			countMaxPlayers();
			calculateOutgoingKilobytesPerSecond();
			calculateIncomingKilobytesPerSecond();
			save();

			try {
				Thread.sleep(configuration.getCalculateMetricsInterval());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void process(PlayerLoggedEvent playerEvent) {
		int playerId = playerEvent.getPlayerId();
		int instanceId = playerEvent.getGameId();
		playerMap.put(playerId, instanceId);
	}

	private void process(PlayerDisconnectedEvent playerEvent) {
		playerMap.remove(playerEvent.getPlayerId());
	}

	private void process(OutgoingSizeMetricEvent event) {
		totalBytesSent += event.getBytesSent();
		totalMessagesSent++;
		averageBytesSent = totalBytesSent / totalMessagesSent;
	}

	private void process(OutgoingTypeMetricEvent event) {
		String type = event.getType();
		Long count = outgoingTypeCountMap.get(type);
		outgoingTypeCountMap.put(type, count == null ? 1 : ++count);
	}

	private void process(IncomingSizeMetricEvent event) {
		totalBytesReceived += event.getBytesReceived();
		totalMessagesReceived++;
		averageBytesReceived = totalBytesReceived / totalMessagesReceived;
	}

	private void process(IncomingTypeMetricEvent event) {
		LOG.trace("Got incoming type metric event");
		String type = event.getType();
		Long count = incomingTypeCountMap.get(type);
		incomingTypeCountMap.put(type, count == null ? 1 : ++count);
	}

	private void calculateOutgoingKilobytesPerSecond() {
		long currentTime = System.currentTimeMillis();
		long timePassedMs = currentTime - lastOutgoingKbsCalculationTime;
		double kilobytesSentSinceLastCheck = (totalBytesSent - totalOutgoingBytesDuringLastCheck) / 1024;
		averageOutgoingKbs = kilobytesSentSinceLastCheck / (timePassedMs / 1000);
		totalOutgoingBytesDuringLastCheck = totalBytesSent;
		lastOutgoingKbsCalculationTime = currentTime;
		LOG.trace("Currently sending [{}] kb/s", averageOutgoingKbs);
	}

	private void calculateIncomingKilobytesPerSecond() {
		long currentTime = System.currentTimeMillis();
		long timePassedMs = currentTime - lastIncomingKbsCalculationTime;
		double kilobytesReceivedSinceLastCheck = (totalBytesReceived - totalIncomingBytesDuringLastCheck) / 1024;
		averageIncomingKbs = kilobytesReceivedSinceLastCheck / (timePassedMs / 1000);
		totalIncomingBytesDuringLastCheck = totalBytesReceived;
		lastIncomingKbsCalculationTime = currentTime;
		LOG.trace("Currently receiving [{}] kb/s", averageIncomingKbs);
	}

	private void countMaxPlayers() {
		int playerCount = playerMap.size();
		if(playerCount > maxPlayersBeforeSave) {
			maxPlayersBeforeSave = playerCount;
		}
		LOG.trace("Currently [{}] players.", playerCount);
	}

	private void saveKbs() {
		repository.saveOutgoingKilobytesPerSecond(averageOutgoingKbs, Instant.now().toEpochMilli());
		repository.saveIncomingKilobytesPerSecond(averageIncomingKbs, Instant.now().toEpochMilli());
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
	public void handleOutgoingTypeMetricEvent(OutgoingTypeMetricEvent event) {
		events.add(event);
	}

	@Subscribe
	public void handleOutgoingSizeMetricEvent(OutgoingSizeMetricEvent event) {
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

	@Subscribe
	public void handleIncomingSizeMetricEvent(IncomingSizeMetricEvent event) {
		events.add(event);
	}

	@Subscribe
	public void handleIncomingTypeMetricEvent(IncomingTypeMetricEvent event) {
		events.add(event);
	}

	public double getAverageOutgoingKbs() {
		return averageOutgoingKbs;
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

	public double getAverageIncomingKbs() {
		return averageIncomingKbs;
	}

	public long getTotalBytesReceived() {
		return totalBytesReceived;
	}

	public double getAverageBytesReceived() {
		return averageBytesReceived;
	}

	public long getTotalMessagesReceived() {
		return totalMessagesReceived;
	}

	public Map<String, Long> getOutgoingTypeCountMap() {
		return outgoingTypeCountMap;
	}

	public Map<String, Long> getIncomingTypeCountMap() {
		return incomingTypeCountMap;
	}

	public Map<Integer, Integer> getPlayerMap() {
		return playerMap;
	}
}
