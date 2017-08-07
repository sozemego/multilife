package soze.multilife.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.events.EventBus;
import soze.multilife.game.Game;
import soze.multilife.game.GameFactory;
import soze.multilife.game.GameRunner;
import soze.multilife.game.Player;
import soze.multilife.game.exceptions.PlayerAlreadyInGameException;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.outgoing.PlayerIdentity;
import soze.multilife.messages.outgoing.PongMessage;
import soze.multilife.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.metrics.events.PlayerLoggedEvent;
import soze.multilife.server.connection.Connection;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lobby. Connected, but not logged in users are stored here,
 * as well as all playing players. This object also starts new games
 * and assigns players to games.
 * This object acts like the messaging hub. It knows which player is in which game,
 * so it can send messages to appropriate game.
 */
public class Lobby implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Lobby.class);

	private final Map<Long, Connection> connections = new ConcurrentHashMap<>();
	private final Map<Long, Integer> playerToGame = new ConcurrentHashMap<>();

	private final GameRunner gameRunner;
	private final GameFactory gameFactory;
	private final EventBus eventBus;

	/**
	 * Object used for locking when a new player is added.
	 */
	private final Object addPlayerLock = new Object();

	public Lobby(EventBus eventBus, GameRunner gameRunner,  GameFactory gameFactory) {
		this.eventBus = Objects.requireNonNull(eventBus);
		this.gameRunner = Objects.requireNonNull(gameRunner);
		this.gameFactory = Objects.requireNonNull(gameFactory);
	}

	public void run() {
		while (true) {

			try {
				Thread.sleep(1000 * 15);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Called when a new player connects.
	 */
	void onConnect(Connection connection) {
		Objects.requireNonNull(connection);
		connections.put(connection.getId(), connection);
		connection.send(getPlayerIdentity(connection.getId()));
	}

	/**
	 * Creates a PersonIdentity object.
	 * This object is sent back to the client.
	 */
	private PlayerIdentity getPlayerIdentity(long id) {
		return new PlayerIdentity(id);
	}

	/**
	 * Called when a player disconnects.
	 */
	void onDisconnect(Connection connection) {
		Objects.requireNonNull(connection);
		long id = connection.getId();
		connections.remove(id);
		Integer gameId = playerToGame.remove(id);
		if(gameId == null) {
			//player was not in any game, so no further action is neccesary
			return;
		}

		Optional<Game> game = gameRunner.getGameById(gameId);

		if(game.isPresent()) {
			try {
				game.get().removePlayer(id);
			} catch (PlayerNotInGameException e) {
				LOG.warn("Trying to remove a player with id [{}] that is not in-game.", e.getPlayerId());
				return;
			}
			eventBus.post(new PlayerDisconnectedEvent(id));
		}
	}

	/**
	 * Handles incoming messages. Id is the connection id.
	 */
	void onMessage(IncomingMessage incMessage, long id) {
		if (incMessage.getType() == IncomingType.PING) {
			connections.get(id).send(new PongMessage());
			return;
		}

		int gameId = playerToGame.get(id);
		gameRunner.getGameById(gameId).ifPresent(game -> {
			try {
				game.acceptMessage(incMessage, id);
			} catch (PlayerNotInGameException e) {
				LOG.warn("Trying to pass a message by a player [{}] who is not in-game.", e.getPlayerId());
			}
		});
	}

	/**
	 * Adds a newly logged player to one of the games.
	 */
	void addPlayer(Player player) {
		LOG.info("Player with name [{}] is trying to login. ", player.getName());

		synchronized (addPlayerLock) {

			Game game = gameRunner.getFreeGame().orElse(gameFactory.createGame());

			try {
				game.addPlayer(player);
			} catch (PlayerAlreadyInGameException e) {
				LOG.warn("Trying to add a player [{}] to a game and this player is already in that game.", e.getPlayerId());
			}

			gameRunner.addGame(game);
			playerToGame.put(player.getId(), game.getId());
			eventBus.post(new PlayerLoggedEvent(player.getId(), game.getId()));
		}
	}

}
