package soze.multilife.metrics.events;

public class PlayerLoggedEvent {

	private final int playerId;
	private final int instanceId;

	public PlayerLoggedEvent(int playerId, int instanceId) {
		this.playerId = playerId;
		this.instanceId = instanceId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getInstanceId() {
		return instanceId;
	}
}
