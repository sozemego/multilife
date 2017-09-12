package soze.multilife.metrics.service;

import java.time.Instant;
import java.util.Map;

public interface MetricsService extends Runnable {

	public double getAverageOutgoingKbs();

	public long getTotalBytesSent();

	public double getAverageBytesSent();

	public long getTotalMessagesSent();

	public double getAverageIncomingKbs();

	public long getTotalBytesReceived();

	public double getAverageBytesReceived();

	public long getTotalMessagesReceived();

	public Map<String, Long> getOutgoingTypeCountMap();

	public Map<String, Long> getIncomingTypeCountMap();

	public Map<Integer, Integer> getPlayerMap();

	Map<Long, Double> getAverageKbsOutgoingSince(Instant timeSince);

	Map<Long, Double> getAverageKbsIncomingSince(Instant timeSince);

}
