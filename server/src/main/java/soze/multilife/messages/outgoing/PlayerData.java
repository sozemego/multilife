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

	private PlayerData(
			Map<Integer, Integer> points,
			Map<Integer, String> names,
			Map<Integer, String> colors
	) {
		this.points = Objects.requireNonNull(points);
		this.names = Objects.requireNonNull(names);
		this.colors = Objects.requireNonNull(colors);
	}
}
