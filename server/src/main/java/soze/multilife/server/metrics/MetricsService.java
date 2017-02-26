package soze.multilife.server.metrics;

import com.google.common.eventbus.Subscribe;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A service for storing, calculating and reporting various metrics.
 */
public class MetricsService implements Runnable {

  private final Queue<MetricEvent> eventQueue = new ConcurrentLinkedQueue<>();

  private long totalBytesSent = 0;
  private double averageBytesSent = 0d;
  private long totalMessagesSent = 0;

  @Override
  public void run() {
	while(true) {

	  MetricEvent event;
	  while((event = eventQueue.poll()) != null) {
		totalBytesSent += event.getBytesSent();
		totalMessagesSent++;
		averageBytesSent = totalBytesSent / totalMessagesSent;
	  }

	}
  }

  @Subscribe
  public void handleMetricEvent(MetricEvent event) {
	eventQueue.add(event);
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

}
