package soze.multilife.game;

import soze.multilife.configuration.GameConfigurationImpl;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Produces Game objects.
 */
public class GameFactory {

	private final Executor executor = Executors.newCachedThreadPool();

	private final GameConfigurationImpl config;
	private final AtomicInteger id = new AtomicInteger(1);

	public GameFactory(GameConfigurationImpl config) {
		this.config = config;
	}

	public Game createGame() {
		BaseGame baseGame = new BaseGame(
			id.getAndIncrement(),
			config.getGridWidth(),
			config.getGridHeight(),
			config.getMaxPlayers(),
			config.getGameDuration(),
			config.getTickRate()
		);
		baseGame.init();
		Game game = new GameRunner(new GamePlayerHandler(new GameIncomingMessageHandler(new GameOutgoingMessageHandler(baseGame))));
		executor.execute(game);
		return game;
	}

}
