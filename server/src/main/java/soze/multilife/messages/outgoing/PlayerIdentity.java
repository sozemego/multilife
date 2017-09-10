package soze.multilife.messages.outgoing;

/**
 * Data about one player.
 */
public class PlayerIdentity extends OutgoingMessage {

	public int playerId;

	public PlayerIdentity(int playerId) {
		setType(OutgoingType.PLAYER_IDENTITY);
		this.playerId = playerId;
	}

	public void accept(OutgoingMessageVisitor visitor) {
		visitor.visit(this);
	}
}
