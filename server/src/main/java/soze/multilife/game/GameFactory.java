package soze.multilife.game;

import soze.multilife.configuration.GameConfigurationImpl;
import soze.multilife.server.GameRunner;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Produces simulation objects.
 */
public class GameFactory {

	private final Executor executor = Executors.newCachedThreadPool();

	private final GameConfigurationImpl config;
	private final AtomicLong id = new AtomicLong(0);

	public GameFactory(GameConfigurationImpl config) {
		this.config = config;
	}

	public Game createGame() {
		Game game = new Game(
			id.getAndIncrement(),
			config.getGridWidth(),
			config.getGridHeight(),
			config.getMaxPlayers(),
			config.getGameDuration(),
			config.getTickRate()
		);
		game.init();
		executor.execute(new GameRunner(game));
		return game;
	}

}
