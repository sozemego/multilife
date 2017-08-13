package soze.multilife.server.gamerunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.configuration.interfaces.GameRunnerConfiguration;
import soze.multilife.game.Game;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This class is responsible for managing the games.
 * It assigns games to threads, creates and manages threads for games
 * and removes finished/inactive games.
 */
public class GameManager {

	private static final Logger LOG = LoggerFactory.getLogger(GameManager.class);

	private final Executor executor = Executors.newCachedThreadPool();
	private final Map<Integer, Game> games = new ConcurrentHashMap<>();
	private final List<GameContainer> gameContainers = Collections.synchronizedList(new ArrayList<>());

	private final int gamesPerThread;
	private final int tickRate;

	public GameManager(GameRunnerConfiguration cfg) {
		this.gamesPerThread = cfg.getGamesPerThread();
		this.tickRate = cfg.getTickRate();
	}

	/**
	 * Adds a new game. Whether a new thread is created for this
	 * game or it's added to an existing thread is decided
	 * on calling this method.
	 */
	public void addGame(Game game) {
		Objects.requireNonNull(game);
		Game previousGame = games.putIfAbsent(game.getId(), game);
		if(previousGame == null) {
			addToContainer(game);
		}
	}

	private void addToContainer(Game game) {
		LOG.trace("Adding a game [{}]", game);
		synchronized (gameContainers) {
			boolean added = false;
			for (GameContainer gameContainer : gameContainers) {
				if(gameContainer.getGamesCount() < this.gamesPerThread) {
					gameContainer.addGame(game);
					added = true;
				}
			}
			if(!added) {
				GameContainer gameContainer = new GameContainer(this.tickRate);
				gameContainers.add(gameContainer);
				gameContainer.addGame(game);
				executor.execute(gameContainer);
			}
		}
	}

	public Optional<Game> getGameById(int id) {
		return Optional.ofNullable(games.get(id));
	}

	/**
	 * Attempts to find a game with a free slot for one player.
	 * If there are no free games, returns an empty optional.
	 */
	public Optional<Game> getFreeGame() {
		for(Game game: games.values()) {
			if(!game.isFull() && !game.isOutOfTime()) {
				return Optional.of(game);
			}
		}
		return Optional.empty();
	}

	public void clearEmptyContainers() {
		gameContainers.removeIf(g -> !g.isRunning());
	}

}
