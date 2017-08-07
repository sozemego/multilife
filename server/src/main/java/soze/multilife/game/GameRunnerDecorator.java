package soze.multilife.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A game layer responsible for updating the game on a timer.
 */
public class GameRunnerDecorator extends GameDecorator {

	private static final Logger LOG = LoggerFactory.getLogger(GameRunnerDecorator.class);

	GameRunnerDecorator(Game game) {
		super(game);
	}

	public void run() {
		LOG.info("Gamerunner for gameId [{}] was started. ", super.getId());
		while (!super.isScheduledForRemoval()) {

			super.run();

			try {
				Thread.sleep(super.getTickRate());
			} catch (InterruptedException e) {
				LOG.error("Instance runner was stopped. ", e);
			}

		}

		super.end();
	}

}
