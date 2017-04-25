package soze.multilife.messages.outgoing;


import java.util.Map;

public class MetricsMessage extends OutgoingMessage {

	private final long totalBytesSent;
	private final double averageBytesPerMessage;
	private final long totalMessagesSent;
	private final Map<String, Long> typeCount;
	private final Map<Long, Long> instancePlayerMap;

	public MetricsMessage(
		long totalBytesSent,
		double averageBytesPerMessage,
		long totalMessagesSent,
		Map<String, Long> typeCount,
		Map<Long, Long> instancePlayerMap
	) {
		setType(OutgoingType.METRICS);
		this.totalBytesSent = totalBytesSent;
		this.averageBytesPerMessage = averageBytesPerMessage;
		this.totalMessagesSent = totalMessagesSent;
		this.typeCount = typeCount;
		this.instancePlayerMap = instancePlayerMap;
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

	public Map<Long, Long> getInstancePlayerMap() {
		return instancePlayerMap;
	}
}