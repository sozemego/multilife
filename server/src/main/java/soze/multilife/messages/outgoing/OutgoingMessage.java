package soze.multilife.messages.outgoing;

public abstract class OutgoingMessage {

	private Type type;

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return this.type;
	}

}
