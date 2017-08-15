package soze.multilife.messages.outgoing;

/**
 * Types of outgoing messages.
 */
public enum OutgoingType {

	CELL_LIST((byte) 1),
	MAP_DATA((byte) 2),
	PLAYER_IDENTITY((byte) 3),
	PLAYER_DATA((byte) 4),
	TICK_DATA((byte) 5),
	PONG((byte) 6),
	TIME_REMAINING((byte) 7),
	METRICS((byte) 8);

	private final byte typeMarker;

	public byte getTypeMarker() {
		return typeMarker;
	}

	public static OutgoingType getType(byte typeMarker) {
		for(OutgoingType type: values()) {
			if(type.getTypeMarker() == typeMarker) {
				return type;
			}
		}

		throw new IllegalStateException("Invalid type marker " + typeMarker);
	}

	OutgoingType(byte typeMarker) {
		this.typeMarker = typeMarker;
	}

}
