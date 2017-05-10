package soze.multilife.messages.outgoing;

import java.util.Map;
import java.util.Objects;

/**
 * Player data for one {@link Game}.
 */
public class PlayerData extends OutgoingMessage {

	public Map<Long, Long> points;
	public Map<Long, String> names;
	public Map<Long, String> colors;
	public Map<Long, String> rules;

	public PlayerData(Map<Long, Long> points, Map<Long, String> names, Map<Long, String> colors, Map<Long, String> rules) {
		setType(OutgoingType.PLAYER_DATA);
		this.points = Objects.requireNonNull(points);
		this.names = Objects.requireNonNull(names);
		this.colors = Objects.requireNonNull(colors);
		this.rules = Objects.requireNonNull(rules);
	}
}
