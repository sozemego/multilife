package soze.multilife.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.Game;

/**
 * A runnable for running games.
 * It updates the game and sleeps.
 */
public class GameRunner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(GameRunner.class);

	private final Game game;

	public GameRunner(Game game) {
		this.game = game;
	}

	@Override
	public void run() {
		LOG.info("Gamerunner for gameId [{}] was started. ", game.getId());
		while (!game.isScheduledForRemoval()) {

			game.update();

			try {
				Thread.sleep(game.getTickRate());
			} catch (InterruptedException e) {
				LOG.error("Instance runner was stopped. ", e);
			}

		}
	}

}
