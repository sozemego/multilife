package soze.multilife.metrics.events;

public class PlayerDisconnectedEvent {

	private final int playerId;

	public PlayerDisconnectedEvent(int playerId) {
		this.playerId = playerId;
	}

	public int getPlayerId() {
		return playerId;
	}
}
