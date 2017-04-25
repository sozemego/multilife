package soze.multilife.messages.outgoing;

/**
 * Data about one player.
 */
public class PlayerIdentity extends OutgoingMessage {

	public long playerId;

	public PlayerIdentity(long playerId) {
		setType(OutgoingType.PLAYER_IDENTITY);
		this.playerId = playerId;
	}
}
