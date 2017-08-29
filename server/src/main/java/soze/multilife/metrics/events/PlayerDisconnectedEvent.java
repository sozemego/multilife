package soze.multilife.metrics.events;

import soze.multilife.metrics.MetricsService;

public class PlayerDisconnectedEvent implements MetricEvent {

	private final int playerId;

	public PlayerDisconnectedEvent(int playerId) {
		this.playerId = playerId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void accept(MetricsService.MetricEventVisitor visitor) {
		visitor.visit(this);
	}
}
