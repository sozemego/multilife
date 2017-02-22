package soze.multilife.messages.outgoing;

public abstract class OutgoingMessage {

	private OutgoingType type;

	public void setType(OutgoingType type) {
		this.type = type;
	}

	public OutgoingType getType() {
		return this.type;
	}

}
