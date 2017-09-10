package soze.multilife.server;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import soze.multilife.events.EventBus;
import soze.multilife.game.Game;
import soze.multilife.game.GameFactory;
import soze.multilife.game.Player;
import soze.multilife.messages.outgoing.PlayerIdentity;
import soze.multilife.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.gamerunner.GameManager;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class LobbyTest {

	@Mock
	private EventBus bus;

	@Mock
	private GameFactory gameFactory;

	@Mock
	private GameManager gameManager;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = NullPointerException.class)
	public void testInvalidConstructor() throws Exception {
		new Lobby(null, null, null);
	}

	@Test
	public void testOnOpen() throws Exception {
		Lobby lobby = new Lobby(bus, gameManager, gameFactory);

		Connection conn = Mockito.mock(Connection.class);
		when(conn.getId()).thenReturn(1);

		lobby.onConnect(conn);
		verify(conn, atLeastOnce()).getId();
		verify(conn, times(1)).send(any(PlayerIdentity.class));
	}

	@Test(expected = NullPointerException.class)
	public void testNullConnectionOnConnect() throws Exception {
		Lobby lobby = new Lobby(bus, gameManager, gameFactory);
		lobby.onConnect(null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullConnectionOnDisconnect() throws Exception {
		Lobby lobby = new Lobby(bus, gameManager, gameFactory);
		lobby.onDisconnect(null);
	}

	@Test
	public void testValidOnDisconnectPlayerConnectedButNotInGame() throws Exception {
		Lobby lobby = new Lobby(bus, gameManager, gameFactory);

		Connection conn = Mockito.mock(Connection.class);
		when(conn.getId()).thenReturn(1);

		lobby.onDisconnect(conn);
		verify(conn, atLeastOnce()).getId();
		verify(bus, times(0)).post(any());
	}

	@Test
	public void testValidOnDisconnectPlayerInGame() throws Exception {

		Game game = Mockito.mock(Game.class);
		when(game.getId()).thenReturn(1);
		when(game.getPlayerColor(1)).thenReturn("#000000");
		when(gameFactory.createGame()).thenReturn(game);
		Lobby lobby = new Lobby(bus, gameManager, gameFactory);

		Connection conn = Mockito.mock(Connection.class);
		when(conn.getId()).thenReturn(1);

		lobby.onConnect(conn);
		Player player = new Player(conn.getId(), conn, "Player");
		when(gameManager.getFreeGame()).thenReturn(Optional.empty());
		lobby.addPlayer(player);

		when(gameManager.getGameById(1)).thenReturn(Optional.of(game));
		lobby.onDisconnect(conn);

		verify(conn, atLeastOnce()).getId();
		verify(bus, times(1)).post(isA(PlayerDisconnectedEvent.class));
	}

	@Test
	public void testValidAddPlayer() throws Exception {

		Game game = Mockito.mock(Game.class);
		when(gameFactory.createGame()).thenReturn(game);
		Lobby lobby = new Lobby(bus, gameManager, gameFactory);

		Connection conn = Mockito.mock(Connection.class);
		when(conn.getId()).thenReturn(1);

		lobby.onConnect(conn);
	}

}