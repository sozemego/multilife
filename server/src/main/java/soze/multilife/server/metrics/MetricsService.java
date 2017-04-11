package soze.multilife.server.metrics;

import com.google.common.eventbus.Subscribe;
import soze.multilife.server.metrics.events.InstanceMetricEvent;
import soze.multilife.server.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.server.metrics.events.PlayerLoggedEvent;
import soze.multilife.server.metrics.events.SerializedMetricEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A service for storing, calculating and reporting various metrics.
 */
public class MetricsService implements Runnable {

  private static final long CALCULATE_METRICS_INTERVAL = 1000 * 60;

  private final Queue<InstanceMetricEvent> instanceMetricEventQueue = new ConcurrentLinkedQueue<>();
  private final Queue<SerializedMetricEvent> serializedMetricEventQueue = new ConcurrentLinkedQueue<>();
  private final Queue<Object> playerEvents = new ConcurrentLinkedQueue<>();

  private long totalBytesSent = 0;
  private double averageBytesSent = 0d;
  private long totalMessagesSent = 0;

  private final Map<String, Long> typeCountMap = new HashMap<>();
  private final Map<Long, Long> playerMap = new HashMap<>();

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
	  while ((instanceMetricEvent = instanceMetricEventQueue.poll()) != null) {
		String type = instanceMetricEvent.getType();
		synchronized (typeCountMap) {
		  Long count = typeCountMap.get(type);
		  typeCountMap.put(type, count == null ? 1 : ++count);
		}
	  }//TODO change those two to be the same as those below?

	  Object playerEvent;
	  while ((playerEvent = playerEvents.poll()) != null) {
		if (playerEvent instanceof PlayerLoggedEvent) {
		  process((PlayerLoggedEvent) playerEvent);
		}
		if (playerEvent instanceof PlayerDisconnectedEvent) {
		  process((PlayerDisconnectedEvent) playerEvent);
		}
	  }

	  try {
	    Thread.sleep(CALCULATE_METRICS_INTERVAL);
	  } catch (InterruptedException e) {
	    e.printStackTrace();
	  }
	}
  }

  private void process(PlayerLoggedEvent playerEvent) {
	synchronized (playerMap) {
	  long playerId = playerEvent.getPlayerId();
	  long instanceId = playerEvent.getInstanceId();
	  playerMap.put(playerId, instanceId);
	}
  }

  private void process(PlayerDisconnectedEvent playerEvent) {
	synchronized (playerMap) {
	  playerMap.remove(playerEvent.getPlayerId());
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

  @Subscribe
  public void handlePlayerLoggedEvent(PlayerLoggedEvent event) {
	playerEvents.add(event);
  }

  @Subscribe
  public void handlePlayerDisconnectedEvent(PlayerDisconnectedEvent event) {
	playerEvents.add(event);
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
