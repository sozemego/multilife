package soze.multilife.messages.outgoing;

public class PlayerRemoved extends OutgoingMessage {

	private final int playerId;

	public PlayerRemoved(int playerId) {
		setType(OutgoingType.PLAYER_REMOVED);
		this.playerId = playerId;
	}

	public void accept(OutgoingMessageVisitor visitor) {
		visitor.visit(this);
	}

	public int getPlayerId() {
		return playerId;
	}

}
