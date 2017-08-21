package soze.multilife.metrics.events;

/**
 * Event fired when a player logs in.
 *
 * Contains this player's id and
 */
public class PlayerLoggedEvent implements MetricEvent {

	private final int playerId;
	private final int gameId;

	public PlayerLoggedEvent(int playerId, int gameId) {
		this.playerId = playerId;
		this.gameId = gameId;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getGameId() {
		return gameId;
	}
}
