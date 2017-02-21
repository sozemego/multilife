package soze.multilife.server;

import soze.multilife.simulation.Player;
import soze.multilife.simulation.Simulation;

import java.util.HashMap;
import java.util.Map;

/**
 * An instance represents one room. Instances contain some players (up to a maximum amount).
 * Instances should be run by InstanceRunners.
 */
public class Instance {

	private final long id;
	private final Simulation simulation;
	private final Map<Long, Player> players = new HashMap<>();
	private final int maxPlayers;
	private boolean scheduledForRemoval;

	public Instance(long id, Simulation simulation, int maxPlayers) {
		this.id = id;
		this.simulation = simulation;
		this.maxPlayers = maxPlayers;
	}

	public void update() {
		simulation.update(players.values());
	}

	public void addPlayer(Player player) {
		players.put(player.getId(), player);
	}

	public void removePlayer(long id) {
		players.remove(id);
	}

	public long getId() {
		return id;
	}

	public boolean isActive() {
		return !players.isEmpty();
	}

	public int getNumberOfPlayers() {
		return players.size();
	}

	public boolean isFull() {
		return getNumberOfPlayers() == maxPlayers;
	}

	public boolean isScheduledForRemoval() {
		return scheduledForRemoval;
	}

	public void setScheduledForRemoval(boolean scheduledForRemoval) {
		this.scheduledForRemoval = scheduledForRemoval;
	}

	public Simulation getSimulation() {
		return simulation;
	}

}
