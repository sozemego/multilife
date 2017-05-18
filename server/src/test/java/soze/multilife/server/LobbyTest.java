package soze.multilife.server;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import soze.multilife.events.EventBus;
import soze.multilife.events.EventBusImpl;
import soze.multilife.game.BaseGame;
import soze.multilife.game.Game;
import soze.multilife.game.GameFactory;
import soze.multilife.game.Player;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.messages.outgoing.PlayerIdentity;
import soze.multilife.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.connection.ConnectionFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LobbyTest {


	@Before
	public void setUp() {

	}

	@Test(expected = NullPointerException.class)
	public void testInvalidConstructor() throws Exception {
		new Lobby(null, null);
	}

	@Test
	public void testOnOpen() throws Exception {
		EventBus bus = Mockito.mock(EventBus.class);
		GameFactory gameFactory = Mockito.mock(GameFactory.class);
		Lobby lobby = new Lobby(bus, gameFactory);

		Connection conn = Mockito.mock(Connection.class);
		when(conn.getId()).thenReturn(1L);

		lobby.onConnect(conn);
		verify(conn, atLeastOnce()).getId();
		verify(conn, times(1)).send(any(PlayerIdentity.class));
	}

	@Test(expected = NullPointerException.class)
	public void testNullConnectionOnConnect() throws Exception {
		EventBus bus = Mockito.mock(EventBus.class);
		GameFactory gameFactory = Mockito.mock(GameFactory.class);
		Lobby lobby = new Lobby(bus, gameFactory);
		lobby.onConnect(null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullConnectionOnDisconnect() throws Exception {
		EventBus bus = Mockito.mock(EventBus.class);
		GameFactory gameFactory = Mockito.mock(GameFactory.class);
		Lobby lobby = new Lobby(bus, gameFactory);
		lobby.onDisconnect(null);
	}

	@Test
	public void testValidOnDisconnectPlayerConnectedButNotInGame() throws Exception {
		EventBus bus = Mockito.mock(EventBus.class);
		GameFactory gameFactory = Mockito.mock(GameFactory.class);
		Lobby lobby = new Lobby(bus, gameFactory);

		Connection conn = Mockito.mock(Connection.class);
		when(conn.getId()).thenReturn(1L);

		lobby.onDisconnect(conn);
		verify(conn, atLeastOnce()).getId();
		verify(bus, times(0)).post(any());
	}

	@Test
	public void testValidOnDisconnectPlayerInGame() throws Exception {
		EventBus bus = Mockito.mock(EventBus.class);
		GameFactory gameFactory = Mockito.mock(GameFactory.class);
		Game game = Mockito.mock(Game.class);
		when(gameFactory.createGame()).thenReturn(game);
		Lobby lobby = new Lobby(bus, gameFactory);

		Connection conn = Mockito.mock(Connection.class);
		when(conn.getId()).thenReturn(1L);

		lobby.onConnect(conn);
		Player player = new Player(conn.getId(), conn, "Player", "BASIC");
		lobby.addPlayer(player);

		lobby.onDisconnect(conn);

		verify(conn, atLeastOnce()).getId();
		verify(bus, times(1)).post(isA(PlayerDisconnectedEvent.class));
	}

	@Test
	public void testValidAddPlayer() throws Exception {
		EventBus bus = Mockito.mock(EventBus.class);
		GameFactory gameFactory = Mockito.mock(GameFactory.class);
		Game game = Mockito.mock(Game.class);
		when(gameFactory.createGame()).thenReturn(game);
		Lobby lobby = new Lobby(bus, gameFactory);

		Connection conn = Mockito.mock(Connection.class);
		when(conn.getId()).thenReturn(1L);

		lobby.onConnect(conn);
	}



}