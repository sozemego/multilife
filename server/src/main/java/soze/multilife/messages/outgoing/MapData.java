package soze.multilife.messages.outgoing;

/**
 * Overall data about one simulation.
 */
public class MapData extends OutgoingMessage {

	public int width;
	public int height;

	public MapData(int width, int height) {
		this.setType(OutgoingType.MAP_DATA);
		this.width = width;
		this.height = height;
	}

}
