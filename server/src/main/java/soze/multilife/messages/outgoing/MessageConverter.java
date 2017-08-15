package soze.multilife.messages.outgoing;

public class MessageConverter {

	public static byte[] convertMessage(PongMessage message) {
		return new byte[] {OutgoingType.PONG.getTypeMarker()};
	}

}
