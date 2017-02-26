package soze.multilife.messages.outgoing;

/**
 * Created by KJurek on 22.02.2017.
 */
public class PlayerIdentity extends OutgoingMessage {

  public long playerId;

  public PlayerIdentity(long playerId) {
	setType(OutgoingType.PLAYER_IDENTITY);
	this.playerId = playerId;
  }
}
