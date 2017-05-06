package soze.multilife.game;

import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.messages.outgoing.PlayerData;

import java.util.Collection;

public class GameDecorator implements Game {

	private final Game game;

	public GameDecorator(Game game) {
		this.game = game;
	}

	public void addPlayer(Player player) {
		game.addPlayer(player);
	}

	public void removePlayer(long id) {
		game.removePlayer(id);
	}

	public void run() {
		game.run();
	}

	public int getId() {
		return game.getId();
	}

	public void acceptMessage(IncomingMessage message, long playerId) {
		game.acceptMessage(message, playerId);
	}

	public void end() {
		game.end();
	}

	public long getTickRate() {
		return game.getTickRate();
	}

	public int getMaxPlayers() {
		return game.getMaxPlayers();
	}

	public PlayerData getPlayerData() {
		return game.getPlayerData();
	}

	public Collection<Player> getPlayers() {
		return game.getPlayers();
	}

	public int getWidth() {
		return game.getWidth();
	}

	public int getHeight() {
		return game.getHeight();
	}

	public Collection<Cell> getAllCells() {
		return game.getAllCells();
	}

	public boolean isOutOfTime() {
		return game.isOutOfTime();
	}

	public boolean isFull() {
		return game.isFull();
	}

	public int getIterations() {
		return game.getIterations();
	}

	public long getRemainingTime() {
		return game.getRemainingTime();
	}

	public Collection<Cell> getClickedCells() {
		return game.getClickedCells();
	}

	public void sendMessage(OutgoingMessage message) {
		game.sendMessage(message);
	}

	public boolean isScheduledForRemoval() {
		return game.isScheduledForRemoval();
	}

}
