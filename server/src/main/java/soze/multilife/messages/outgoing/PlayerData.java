package soze.multilife.messages.outgoing;

import soze.multilife.server.Instance;

import java.util.Map;

/**
 * Contains player data for one {@link Instance}.
 */
public class PlayerData extends OutgoingMessage {

  public Map<Long, Long> points;
  public Map<Long, String> names;
  public Map<Long, String> colors;
  public Map<Long, String> rules;

  public PlayerData(Map<Long, Long> points, Map<Long, String> names, Map<Long, String> colors, Map<Long, String> rules) {
    setType(OutgoingType.PLAYER_DATA);
	this.points = points;
	this.names = names;
	this.colors = colors;
	this.rules = rules;
  }
}