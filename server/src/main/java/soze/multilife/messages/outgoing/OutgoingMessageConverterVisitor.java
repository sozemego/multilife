package soze.multilife.messages.outgoing;

public class OutgoingMessageConverterVisitor implements OutgoingMessageVisitor {

	private byte[] payload;

	public void visit(CellList message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);	}

	public void visit(MapData message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);
	}

	public void visit(PlayerAdded message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);
	}

	public void visit(PlayerIdentity message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);
	}

	public void visit(PlayerPoints message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);
	}

	public void visit(PlayerRemoved message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);
	}

	public void visit(PongMessage message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);
	}

	public void visit(TickData message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);
	}

	public void visit(TimeRemainingMessage message) {
		this.payload = OutgoingMessageConverter.convertMessage(message);
	}

	public byte[] getPayload() {
		return this.payload;
	}
}
