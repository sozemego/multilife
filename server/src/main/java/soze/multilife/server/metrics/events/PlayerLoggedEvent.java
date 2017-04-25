package soze.multilife.server.metrics.events;

public class PlayerLoggedEvent {

	private final long playerId;
	private final long instanceId;

	public PlayerLoggedEvent(long playerId, long instanceId) {
		this.playerId = playerId;
		this.instanceId = instanceId;
	}

	public long getPlayerId() {
		return playerId;
	}

	public long getInstanceId() {
		return instanceId;
	}
}
