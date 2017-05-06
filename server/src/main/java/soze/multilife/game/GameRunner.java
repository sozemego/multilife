package soze.multilife.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.messages.outgoing.PlayerData;

import java.util.Collection;

/**
 * A game layer responsible for updating the game on a timer.
 */
public class GameRunner implements Game {

	private static final Logger LOG = LoggerFactory.getLogger(GameRunner.class);

	private final Game game;

	GameRunner(Game game) {
		this.game = game;
	}

	public void run() {
		LOG.info("Gamerunner for gameId [{}] was started. ", game.getId());
		while (!game.isScheduledForRemoval()) {

			game.run();

			try {
				Thread.sleep(game.getTickRate());
			} catch (InterruptedException e) {
				LOG.error("Instance runner was stopped. ", e);
			}

		}

		game.end();

	}

	//
	//
	// DELEGATED METHODS
	//
	//

	public int getId() {
		return game.getId();
	}

	public int getMaxPlayers() {
		return game.getMaxPlayers();
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

	public void end() {
		game.end();
	}

	public long getTickRate() {
		return game.getTickRate();
	}

	public PlayerData getPlayerData() {
		return game.getPlayerData();
	}

	public Collection<Player> getPlayers() {
		return game.getPlayers();
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

	public void addPlayer(Player player) {
		game.addPlayer(player);
	}

	public void removePlayer(long id) {
		game.removePlayer(id);
	}

	public void sendMessage(OutgoingMessage message) {
		game.sendMessage(message);
	}

	public boolean isScheduledForRemoval() {
		return game.isScheduledForRemoval();
	}

	public void acceptMessage(IncomingMessage message, long playerId) {
		game.acceptMessage(message, playerId);
	}

	//
	//
	// END DELEGATED METHODS
	//
	//

}
