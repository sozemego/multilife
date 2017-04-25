package soze.multilife.messages.outgoing;

/**
 * Created by soze on 3/5/2017.
 */
public class PongMessage extends OutgoingMessage {

	public PongMessage() {
		setType(OutgoingType.PONG);
	}
}
