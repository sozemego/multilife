package soze.multilife.messages.outgoing;


import java.util.Map;

public class MetricsMessage extends OutgoingMessage {

	private final double averageKbs;
	private final long totalBytesSent;
	private final double averageBytesPerMessage;
	private final long totalMessagesSent;
	private final Map<String, Long> typeCount;
	private final Map<Integer, Integer> instancePlayerMap;

	public MetricsMessage(
		double averageKbs,
		long totalBytesSent,
		double averageBytesPerMessage,
		long totalMessagesSent,
		Map<String, Long> typeCount,
		Map<Integer, Integer> instancePlayerMap
	) {
		setType(OutgoingType.METRICS);
		this.averageKbs = averageKbs;
		this.totalBytesSent = totalBytesSent;
		this.averageBytesPerMessage = averageBytesPerMessage;
		this.totalMessagesSent = totalMessagesSent;
		this.typeCount = typeCount;
		this.instancePlayerMap = instancePlayerMap;
	}

	public double getAverageKbs() {
		return averageKbs;
	}

	public long getTotalBytesSent() {
		return totalBytesSent;
	}

	public double getAverageBytesPerMessage() {
		return averageBytesPerMessage;
	}

	public long getTotalMessagesSent() {
		return totalMessagesSent;
	}

	public Map<String, Long> getTypeCount() {
		return typeCount;
	}

	public Map<Integer, Integer> getInstancePlayerMap() {
		return instancePlayerMap;
	}
}