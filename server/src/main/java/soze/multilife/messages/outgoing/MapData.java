package soze.multilife.messages.outgoing;

import java.util.Map;

/**
 * Created by KJurek on 22.02.2017.
 */
public class MapData extends OutgoingMessage {

  public int width;
  public int height;
  public Map<Long, String> playerColors;

  public MapData(int width, int height, Map<Long, String> playerColors) {
    this.setType(OutgoingType.MAP_DATA);
    this.width = width;
    this.height = height;
    this.playerColors = playerColors;
  }

}
