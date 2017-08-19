package soze.multilife.messages.outgoing;

public class PlayerPoints extends OutgoingMessage {

	private final int playerId;
	private final int playerPoints;

	public PlayerPoints(int playerId, int playerPoints) {
		setType(OutgoingType.PLAYER_POINTS);
		this.playerId = playerId;
		this.playerPoints = playerPoints;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getPlayerPoints() {
		return playerPoints;
	}
}
