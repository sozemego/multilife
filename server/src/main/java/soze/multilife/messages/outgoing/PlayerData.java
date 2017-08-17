package soze.multilife.messages.outgoing;

import java.util.Map;
import java.util.Objects;

/**
 * Player data for one {@link soze.multilife.game.BaseGame}.
 */
public class PlayerData extends OutgoingMessage {

	public Map<Integer, Integer> points;
	public Map<Integer, String> names;
	public Map<Integer, String> colors;
	public Map<Integer, String> rules;

	public PlayerData(
			Map<Integer, Integer> points,
			Map<Integer, String> names,
			Map<Integer, String> colors,
			Map<Integer, String> rules
	) {
		setType(OutgoingType.PLAYER_DATA);
		this.points = Objects.requireNonNull(points);
		this.names = Objects.requireNonNull(names);
		this.colors = Objects.requireNonNull(colors);
		this.rules = Objects.requireNonNull(rules);
	}
}
