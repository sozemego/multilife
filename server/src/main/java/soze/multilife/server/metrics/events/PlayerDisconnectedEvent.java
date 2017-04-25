package soze.multilife.server.metrics.events;

public class PlayerDisconnectedEvent {

	private final long playerId;

	public PlayerDisconnectedEvent(long playerId) {
		this.playerId = playerId;
	}

	public long getPlayerId() {
		return playerId;
	}
}
