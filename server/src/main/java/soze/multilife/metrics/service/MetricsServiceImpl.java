package soze.multilife.metrics.service;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.configuration.interfaces.MetricsConfiguration;
import soze.multilife.metrics.events.*;
import soze.multilife.metrics.repository.MetricsRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A service for storing, calculating and reporting various metrics.
 */
public class MetricsServiceImpl implements MetricsService {

  private static final Logger LOG = LoggerFactory.getLogger(MetricsServiceImpl.class);

  private final MetricsRepository repository;
  private final MetricsConfiguration configuration;
  private final MetricEventVisitor metricEventVisitor;

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

  public MetricsServiceImpl(MetricsRepository repository, MetricsConfiguration configuration) {
    this.repository = Objects.requireNonNull(repository);
    this.configuration = Objects.requireNonNull(configuration);
    this.metricEventVisitor = new MetricEventVisitor();
  }

  public void run() {
    while (true) {

      Object event;
      while ((event = events.poll()) != null) {
        ((MetricEvent) event).accept(this.metricEventVisitor);
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
    if (playerCount > maxPlayersBeforeSave) {
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
    if (currentTime > lastSaveTime + configuration.metricsSaveInterval()) {
      saveKbs();
      saveMaxPlayers();
      lastSaveTime = currentTime;
    }
  }

  @Subscribe
  @AllowConcurrentEvents
  public void handleMetricEvent(MetricEvent event) {
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

  /**
   * Maximum amount of time/kbs value pairs retrieved by
   * <br>
   * {@link MetricsService#getAverageKbsOutgoingSince}
   * <br>
   * {@link MetricsService#getAverageKbsIncomingSince}
   */
  private static final int MAX_AVERAGE_KBS_COUNT = 500;

  public Map<Long, Double> getAverageKbsOutgoingSince(Instant timeSince) {
    Map<Long, Double> data = repository.getAverageKbsIncomingSince(timeSince);
    return thinData(data, MAX_AVERAGE_KBS_COUNT);
  }

  public Map<Long, Double> getAverageKbsIncomingSince(Instant timeSince) {
    Map<Long, Double> data = repository.getAverageKbsIncomingSince(timeSince);
    return thinData(data, MAX_AVERAGE_KBS_COUNT);
  }

  private Map<Long, Double> thinData(Map<Long, Double> data, int maxCount) {
    if (data.size() < maxCount) {
      return data;
    }
    int index = 0;
    List<Long> keysToRemove = new ArrayList<>(maxCount);
    int leaveNthElement = (int) Math.ceil(data.size() / maxCount);
    for (Map.Entry<Long, Double> entry : data.entrySet()) {
      if (index % leaveNthElement != 0) {
        keysToRemove.add(entry.getKey());
      }
      index++;
    }
    keysToRemove.forEach(data::remove);
    return data;
  }

  /**
   * Inner helper class for handling {@link MetricEvent}s.
   */
  public class MetricEventVisitor {

    public void visit(IncomingSizeMetricEvent event) {
      totalBytesReceived += event.getBytesReceived();
      totalMessagesReceived++;
      averageBytesReceived = totalBytesReceived / totalMessagesReceived;
    }

    public void visit(IncomingTypeMetricEvent event) {
      String type = event.getType();
      Long count = incomingTypeCountMap.get(type);
      incomingTypeCountMap.put(type, count == null ? 1 : ++count);
    }

    public void visit(OutgoingSizeMetricEvent event) {
      totalBytesSent += event.getBytesSent();
      totalMessagesSent++;
      averageBytesSent = totalBytesSent / totalMessagesSent;
    }

    public void visit(OutgoingTypeMetricEvent event) {
      String type = event.getType();
      Long count = outgoingTypeCountMap.get(type);
      outgoingTypeCountMap.put(type, count == null ? 1 : ++count);
    }

    public void visit(PlayerDisconnectedEvent event) {
      playerMap.remove(event.getPlayerId());
    }

    public void visit(PlayerLoggedEvent event) {
      int playerId = event.getPlayerId();
      int instanceId = event.getGameId();
      playerMap.put(playerId, instanceId);
    }

  }
}
