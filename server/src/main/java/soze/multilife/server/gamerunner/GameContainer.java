package soze.multilife.server.gamerunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.Game;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class GameContainer implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(GameContainer.class);

	private final Map<Integer, Game> games = new ConcurrentHashMap<>();

	private final long tickRate;

	private boolean isRunning = true;

	public GameContainer(long tickRate) {
		this.tickRate = tickRate;
	}

	public void addGame(Game game) {
		Objects.requireNonNull(game);
		games.put(game.getId(), game);
	}

	public int getGamesCount() {
		return games.size();
	}

	public void stop() {
		this.isRunning = false;
	}

	public void run() {
		while(isRunning) {

			long startTime = 0;

			if(LOG.isTraceEnabled()) {
				startTime = System.nanoTime();
			}

			games.values().removeIf(Game::isScheduledForRemoval);
			games.values().forEach(Game::run);

			if(LOG.isTraceEnabled()) {
				long totalTime = System.nanoTime() - startTime;
				LOG.trace("It took [{}] ms to run [{}] games", totalTime / 1e6, games.size());
			}

			try {
				Thread.sleep(this.tickRate);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
