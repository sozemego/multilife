package soze.multilife.game;

import soze.multilife.configuration.interfaces.GameConfiguration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Produces Game objects.
 */
public class GameFactory {

	private final GameConfiguration config;
	private final AtomicInteger id = new AtomicInteger(1);

	public GameFactory(GameConfiguration config) {
		this.config = config;
	}

	public Game createGame() {
		BaseGame baseGame = new BaseGame(
			id.getAndIncrement(),
			config.getInitialDensity(),
			config.getGridWidth(),
			config.getGridHeight(),
			config.getMaxPlayers(),
			config.getGameDuration(),
			config.getTickRate()
		);
		return new GameRunnerDecorator(
				new GamePlayerHandler(
						new GameIncomingMessageQueue(
								new GameOutgoingMessageHandler(baseGame))));
	}

	private static class NewGameConfigBundle {

	}

}
