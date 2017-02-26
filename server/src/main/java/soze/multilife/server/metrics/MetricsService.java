package soze.multilife.server.metrics;

import com.google.common.eventbus.Subscribe;
import soze.multilife.server.metrics.events.InstanceMetricEvent;
import soze.multilife.server.metrics.events.SerializedMetricEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A service for storing, calculating and reporting various metrics.
 */
public class MetricsService implements Runnable {

  private final Queue<InstanceMetricEvent> instanceMetricEventQueue = new ConcurrentLinkedQueue<>();
  private final Queue<SerializedMetricEvent> serializedMetricEventQueue = new ConcurrentLinkedQueue<>();

  private long totalBytesSent = 0;
  private double averageBytesSent = 0d;
  private long totalMessagesSent = 0;

  private final Map<String, Long> typeCountMap = new HashMap<>();

  @Override
  public void run() {
	while (true) {

	  SerializedMetricEvent serializedMetricEvent;
	  while ((serializedMetricEvent = serializedMetricEventQueue.poll()) != null) {
		totalBytesSent += serializedMetricEvent.getBytesSent();
		totalMessagesSent++;
		averageBytesSent = totalBytesSent / totalMessagesSent;
	  }

	  InstanceMetricEvent instanceMetricEvent;
	  while((instanceMetricEvent = instanceMetricEventQueue.poll()) != null) {
		String type = instanceMetricEvent.getType();
		synchronized (typeCountMap) {
		  Long count = typeCountMap.get(type);
		  typeCountMap.put(type, count == null ? 1 : ++count);
		}
	  }

	}
  }

  @Subscribe
  public void handleInstanceMetricEvent(InstanceMetricEvent event) {
	instanceMetricEventQueue.add(event);
  }

  @Subscribe
  public void handleSerializedMetricEvent(SerializedMetricEvent event) {
	serializedMetricEventQueue.add(event);
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
}
