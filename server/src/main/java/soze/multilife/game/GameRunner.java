package soze.multilife.game;

import soze.multilife.configuration.interfaces.GameRunnerConfiguration;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This class is responsible for running the games.
 * It assigns games to threads, creates and manages threads for games
 * and removes finished/inactive games.
 */
public class GameRunner {

	private final Executor executor = Executors.newCachedThreadPool();
	private final Map<Integer, Game> games = new ConcurrentHashMap<>();

	private final int gamesPerThread;

	public GameRunner(GameRunnerConfiguration cfg) {
		this.gamesPerThread = cfg.getGamesPerThread();
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
			executor.execute(game);
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

}
