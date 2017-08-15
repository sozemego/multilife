package soze.multilife.server.gamerunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.Game;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.IncomingMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameContainer implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(GameContainer.class);

	private final long id;

	private final Map<Integer, Game> games = new ConcurrentHashMap<>();

	private final long tickRate;

	private boolean isRunning = true;

	private final Queue<MessageQueueNode> queuedMessages = new ConcurrentLinkedQueue<>();

	public GameContainer(long id, long tickRate) {
		this.id = id;
		this.tickRate = tickRate;
	}

	public long getId() {
		return id;
	}

	public void addGame(Game game) {
		if(!isRunning()) {
			throw new IllegalStateException("Cannot add a game to a game container which is not running!");
		}
		Objects.requireNonNull(game);
		games.put(game.getId(), game);
	}

	public int getGamesCount() {
		return games.size();
	}

	public void stop() {
		this.isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void acceptMessage(IncomingMessage message, long playerId, int gameId) {
		queuedMessages.add(new MessageQueueNode(message, playerId, gameId));
	}

	private void handleMessages() {
		MessageQueueNode node;
		while ((node = queuedMessages.poll()) != null) {
			IncomingMessage message = node.getIncomingMessage();
			long id = node.getPlayerId();
			Game game = games.get(node.getGameId());
			if(game != null) {
				try {
					game.acceptMessage(message, id);
				} catch (PlayerNotInGameException e) {
					LOG.warn("Trying to pass a message to a player who is not in game. ");
				}
			}

		}
	}

	public void run() {
		while(isRunning) {

			long startTime = 0;

			if(LOG.isTraceEnabled()) {
				startTime = System.nanoTime();
			}

			handleMessages();

			games.values()
					.stream()
					.filter(Game::isOutOfTime)
					.forEach(Game::end);

			games.values()
					.removeIf(Game::isScheduledForRemoval);

			games.values()
					.forEach(Game::run);

			if(LOG.isTraceEnabled()) {
				long totalTime = System.nanoTime() - startTime;
				LOG.trace("It took [{}] ms to run [{}] games", totalTime / 1e6, games.size());
			}

			try {
				Thread.sleep(this.tickRate);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if(games.isEmpty()) {
				stop();
			}
		}
	}
}