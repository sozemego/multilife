package soze.multilife.game;

import soze.multilife.game.exceptions.PlayerAlreadyInGameException;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.messages.outgoing.PlayerData;

import java.awt.*;
import java.util.Collection;
import java.util.Map;

public class GameDecorator implements Game {

	private final Game game;

	public GameDecorator(Game game) {
		this.game = game;
	}

	public boolean addPlayer(Player player) throws PlayerAlreadyInGameException {
		return game.addPlayer(player);
	}

	public void removePlayer(long id) throws PlayerNotInGameException {
		game.removePlayer(id);
	}

	public int getId() {
		return game.getId();
	}

	public void acceptMessage(IncomingMessage message, long playerId) throws PlayerNotInGameException {
		game.acceptMessage(message, playerId);
	}

	public void end() {
		game.end();
	}

	public int getMaxPlayers() {
		return game.getMaxPlayers();
	}

	public PlayerData getPlayerData() {
		return game.getPlayerData();
	}

	public Map<Long, Player> getPlayers() {
		return game.getPlayers();
	}

	public int getWidth() {
		return game.getWidth();
	}

	public int getHeight() {
		return game.getHeight();
	}

	public Map<Point, Cell> getAllCells() {
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

	public void run() {
		game.run();
	}

}
