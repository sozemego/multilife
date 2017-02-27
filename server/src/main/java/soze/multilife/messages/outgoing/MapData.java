package soze.multilife.messages.outgoing;

/**
 * Created by KJurek on 22.02.2017.
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
