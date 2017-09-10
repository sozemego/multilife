package soze.multilife.messages.outgoing;


import java.util.Map;

public class MetricsMessage extends OutgoingMessage {

	private final double averageOutgoingKbs;
	private final long totalBytesSent;
	private final double averageBytesPerOutgoingMessage;
	private final long totalMessagesSent;

	private final double averageIncomingKbs;
	private final long totalBytesReceived;
	private final double averageBytesPerIncomingMessage;
	private final long totalMessagesReceived;

	private final Map<String, Long> outgoingTypeCount;
	private final Map<String, Long> incomingTypeCount;
	private final Map<Integer, Integer> instancePlayerMap;

	public MetricsMessage(
			double averageOutgoingKbs,
			long totalBytesSent,
			double averageBytesPerOutgoingMessage,
			long totalMessagesSent,
			double averageIncomingKbs,
			long totalBytesReceived,
			double averageBytesPerIncomingMessage,
			long totalMessagesReceived,
			Map<String, Long> outgoingTypeCount,
			Map<String, Long> incomingTypeCount,
			Map<Integer, Integer> instancePlayerMap
	) {
		setType(OutgoingType.METRICS);
		this.incomingTypeCount = incomingTypeCount;
		this.averageIncomingKbs = averageIncomingKbs;
		this.totalBytesReceived = totalBytesReceived;
		this.averageBytesPerIncomingMessage = averageBytesPerIncomingMessage;
		this.totalMessagesReceived = totalMessagesReceived;
		this.averageOutgoingKbs = averageOutgoingKbs;
		this.totalBytesSent = totalBytesSent;
		this.averageBytesPerOutgoingMessage = averageBytesPerOutgoingMessage;
		this.totalMessagesSent = totalMessagesSent;
		this.outgoingTypeCount = outgoingTypeCount;
		this.instancePlayerMap = instancePlayerMap;
	}

	public double getAverageOutgoingKbs() {
		return averageOutgoingKbs;
	}

	public long getTotalBytesSent() {
		return totalBytesSent;
	}

	public double getAverageBytesPerOutgoingMessage() {
		return averageBytesPerOutgoingMessage;
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

	public double getAverageBytesPerIncomingMessage() {
		return averageBytesPerIncomingMessage;
	}

	public long getTotalMessagesReceived() {
		return totalMessagesReceived;
	}

	public Map<String, Long> getOutgoingTypeCount() {
		return outgoingTypeCount;
	}

	public Map<Integer, Integer> getInstancePlayerMap() {
		return instancePlayerMap;
	}

	public Map<String, Long> getIncomingTypeCount() {
		return incomingTypeCount;
	}

	public void accept(OutgoingMessageVisitor visitor) {
		//noop
	}
}