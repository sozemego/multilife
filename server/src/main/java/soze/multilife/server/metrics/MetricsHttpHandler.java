package soze.multilife.server.metrics;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;

import java.util.Collection;
import java.util.Map;

/**
 * A http handler for /metrics endpoint.
 */
public class MetricsHttpHandler implements HttpHandler {

  private final MetricsService metricsService;

  public MetricsHttpHandler(MetricsService metricsService) {
	this.metricsService = metricsService;
  }

  @Override
  public void handleHttpRequest(HttpRequest request, HttpResponse response, HttpControl control) throws Exception {
	response.header("Content-type", "text-html")
	  .content(createResponse())
	  .end();
  }

  private String createResponse() {
	long totalBytesSent = metricsService.getTotalBytesSent();
	long messagesSent = metricsService.getTotalMessagesSent();
	double averageBytesSent = metricsService.getAverageBytesSent();

	Map<String, Long> typeCountMap = metricsService.getTypeCountMap();

	String typeCountMapString = "";

	synchronized (typeCountMap) {
	  for (Map.Entry<String, Long> entry : typeCountMap.entrySet()) {
		typeCountMapString += "Type: " + entry.getKey() + " -> [" + entry.getValue() + "]\n";
	  }
	}

	Map<Long, Long> playerMap = metricsService.getPlayerMap();
	String playerMapString = "";

	synchronized (playerMap) {
	  Multimap<Long, Long> instancePlayerMap = HashMultimap.create();
	  for (Map.Entry<Long, Long> entry : playerMap.entrySet()) {
		long playerId = entry.getKey();
		long instanceId = entry.getValue();
		instancePlayerMap.put(instanceId, playerId);
	  }
	  for (Map.Entry<Long, Collection<Long>> entry : instancePlayerMap.asMap().entrySet()) {
		long instanceId = entry.getKey();
		Collection<Long> players = entry.getValue();
		playerMapString += "Instance [" + instanceId + "] players [" + players.size() + "]\n";
	  }
	}

	return "Total bytes sent: " + totalBytesSent +
	  ".\nTotal megabytes sent: " + (totalBytesSent / 1e6) +
	  ".\nAverage bytes per message: " + averageBytesSent +
	  ".\nTotal messages sent: " + messagesSent +
	  ".\n" + typeCountMapString +
	  ".\n" + playerMapString;
  }


}
