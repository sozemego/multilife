package soze.multilife.messages.incoming;

/**
 * Created by soze on 2/21/2017.
 */
public enum IncomingType {

	LOGIN((byte) 1),
	CLICK((byte) 2),
	PING((byte) 3);

	private final byte typeMarker;

	public byte getTypeMarker() {
		return typeMarker;
	}

	public static IncomingType getType(byte typeMarker) {
		for(IncomingType type: values()) {
			if(type.getTypeMarker() == typeMarker) {
				return type;
			}
		}

		throw new IllegalStateException("Invalid type marker " + typeMarker);
	}

	IncomingType(byte typeMarker) {
		this.typeMarker = typeMarker;
	}

}
