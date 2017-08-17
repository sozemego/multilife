package soze.multilife.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.events.EventBus;
import soze.multilife.game.Cell;
import soze.multilife.game.Game;
import soze.multilife.game.GameFactory;
import soze.multilife.game.Player;
import soze.multilife.game.exceptions.PlayerAlreadyInGameException;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.outgoing.*;
import soze.multilife.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.metrics.events.PlayerLoggedEvent;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.gamerunner.GameManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
	private final Multimap<Integer, Player> gameToPlayers = Multimaps.synchronizedMultimap(ArrayListMultimap.create());

	private final GameManager gameManager;
	private final GameFactory gameFactory;
	private final EventBus eventBus;

	/**
	 * Object used for locking when a new player is added.
	 */
	private final Object addPlayerLock = new Object();

	public Lobby(EventBus eventBus, GameManager gameManager, GameFactory gameFactory) {
		this.eventBus = Objects.requireNonNull(eventBus);
		this.gameManager = Objects.requireNonNull(gameManager);
		this.gameFactory = Objects.requireNonNull(gameFactory);
	}

	public void run() {
		while (true) {

			gameManager.clearEmptyContainers();

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

		gameToPlayers.removeAll(gameId);

		Optional<Game> game = gameManager.getGameById(gameId);
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
	 * Handles incoming messages. Id is the connection connectionId.
	 */
	void onMessage(IncomingMessage incMessage, long connectionId) {
		if (incMessage.getType() == IncomingType.PING) {
			connections.get(connectionId).send(new PongMessage());
			return;
		}

		int gameId = playerToGame.get(connectionId);
		gameManager.acceptMessage(incMessage, connectionId, gameId);
	}

	/**
	 * Adds a newly logged player to one of the games.
	 */
	void addPlayer(Player player) {
		LOG.info("Player with name [{}] is trying to login. ", player.getName());

		synchronized (addPlayerLock) {

			Game game = gameManager.getFreeGame().orElse(gameFactory.createGame());

			try {
				game.addPlayer(player);
			} catch (PlayerAlreadyInGameException e) {
				LOG.warn("Trying to add a player [{}] to a game and this player is already in that game.", e.getPlayerId());
			}

			gameManager.addGame(game);
			playerToGame.put(player.getId(), game.getId());
			eventBus.post(new PlayerLoggedEvent(player.getId(), game.getId()));
			gameToPlayers.put(game.getId(), player);

			Collection<Player> players = gameToPlayers.get(game.getId());
			players.forEach(p -> {
				p.send(game.getPlayerData());
				p.send(new MapData(game.getWidth(), game.getHeight()));
				p.send(getAllAliveCellData(game));
			});
		}
	}

	/**
	 * Assembles {@link CellList} from all alive cells
	 * of this game.
	 *
	 */
	private CellList getAllAliveCellData(Game game) {
		List<Cell> cells = game.getAllCells().values().stream()
				.filter(Cell::isAlive)
				.collect(Collectors.toList());
		return constructCellList(cells);
	}

	/**
	 * Assembles {@link CellList} from a given list of cells.
	 *
	 * @param cells cells
	 * @return CellList
	 */
	private CellList constructCellList(Collection<Cell> cells) {
		List<CellData> cellData = new ArrayList<>();
		for (Cell cell : cells) {
			cellData.add(new CellData(cell));
		}
		return new CellList(cellData);
	}

}
