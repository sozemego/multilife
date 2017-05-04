package soze.multilife.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.events.EventHandler;
import soze.multilife.game.Game;
import soze.multilife.game.GameFactory;
import soze.multilife.game.Player;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.messages.outgoing.PlayerIdentity;
import soze.multilife.messages.outgoing.PongMessage;
import soze.multilife.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.metrics.events.PlayerLoggedEvent;
import soze.multilife.server.connection.Connection;

import java.util.HashMap;
import java.util.Map;

/**
 * A lobby. Connected, but not logged in users are stored here,
 * as well as all playing players. This object also starts new games
 * and assigns players to games.
 * It also passes messages along to appropriate games.
 */
public class Lobby implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Lobby.class);

	private final Map<Long, Connection> connections = new HashMap<>();
	private final Map<Long, Game> games = new HashMap<>();
	private final Map<Long, Long> playerToGame = new HashMap<>();
	private final GameFactory gameFactory;

	private final EventHandler eventHandler;

	public Lobby(EventHandler eventHandler, GameFactory gameFactory) {
		this.eventHandler = eventHandler;
		this.gameFactory = gameFactory;
	}

	@Override
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
	 *
	 * @param connection
	 */
	void onConnect(Connection connection) {
		connections.put(connection.getId(), connection);
		connection.send(getPlayerIdentity(connection.getId()));
	}

	/**
	 * Creates a PersonIdentity object.
	 *
	 * @param id unique id of the player
	 * @return
	 */
	private PlayerIdentity getPlayerIdentity(long id) {
		return new PlayerIdentity(id);
	}

	/**
	 * Called when a player disconnects.
	 *
	 * @param connection
	 */
	void onDisconnect(Connection connection) {
		long id = connection.getId();
		connections.remove(id);
		long gameId = playerToGame.remove(id);
		games.get(gameId).removePlayer(id);
		eventHandler.post(new PlayerDisconnectedEvent(id));
	}

	/**
	 * Handles incoming messages. Id is the connection id.
	 *
	 * @param incMessage
	 * @param id
	 */
	void onMessage(IncomingMessage incMessage, long id) {
		if (incMessage.getType() == IncomingType.LOGIN) {
			handleLoginMessage((LoginMessage) incMessage, id);
			return;
		}
		if (incMessage.getType() == IncomingType.PING) {
			connections.get(id).send(new PongMessage());
			return;
		}
		long gameId = playerToGame.get(id);
		Game game = games.get(gameId);
		game.addMessage(incMessage, id);
	}

	/**
	 * Handles login message. Finds an instance with free space
	 * or creates a new once.
	 *
	 * @param message
	 * @param id
	 */
	private void handleLoginMessage(LoginMessage message, long id) {
		LOG.info("Player with name [{}] is trying to login. ", message.getName());

		Game game;
		//1. find free, active room.
		synchronized (games) {
			for(Game g: games.values()) {
				if(!g.isFull() && !g.isOutOfTime()) {
					game = g;
					break;
				}
			}
		}

		//2. if no free or active rooms, create a new one
		game = gameFactory.createGame();
		Player player = createPlayer(connections.get(id), message.getName(), "BASIC");
		game.addPlayer(player);

		synchronized (games) {
			games.put(game.getId(), game);
		}
		playerToGame.put(id, game.getId());
		eventHandler.post(new PlayerLoggedEvent(id, game.getId()));
	}

	private Player createPlayer(Connection connection, String name, String rule) {
		return new Player(connection.getId(), connection, name, rule);
	}

}
