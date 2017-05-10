package soze.multilife.messages.incoming;

public class ClickMessage extends IncomingMessage {

	public int[] indices;

	public ClickMessage() {
		setType(IncomingType.CLICK);
	}

	public int[] getIndices() {
		return indices;
	}

	public void setIndices(int[] indices) {
		this.indices = indices;
	}
}
