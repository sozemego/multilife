package soze.multilife.metrics.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class NullMetricsService implements MetricsService {

  public double getAverageOutgoingKbs() {
    return 0;
  }

  public long getTotalBytesSent() {
    return 0;
  }

  public double getAverageBytesSent() {
    return 0;
  }

  public long getTotalMessagesSent() {
    return 0;
  }

  public double getAverageIncomingKbs() {
    return 0;
  }

  public long getTotalBytesReceived() {
    return 0;
  }

  public double getAverageBytesReceived() {
    return 0;
  }

  public long getTotalMessagesReceived() {
    return 0;
  }

  public Map<String, Long> getOutgoingTypeCountMap() {
    return null;
  }

  public Map<String, Long> getIncomingTypeCountMap() {
    return new HashMap<>();
  }

  public Map<Integer, Integer> getPlayerMap() {
    return new HashMap<>();
  }

  @Override
  public Map<Long, Double> getAverageKbsOutgoingSince(Instant instant) {
    return new HashMap<>();
  }

  @Override
  public Map<Long, Double> getAverageKbsIncomingSince(Instant timeSince) {
    return new HashMap<>();
  }

  public void run() {

  }
}
