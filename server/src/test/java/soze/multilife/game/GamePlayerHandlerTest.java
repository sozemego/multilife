package soze.multilife.game;

import org.junit.Test;
import soze.multilife.game.exceptions.PlayerAlreadyInGameException;
import soze.multilife.game.exceptions.PlayerNotInGameException;

import static org.junit.Assert.*;

public class GamePlayerHandlerTest extends GameTest {

	@Test
	public void testAddPlayerBeforeRun() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().build());

		Player player = createPlayerMock(1L);
		game.addPlayer(player);
		//GamePlayerHandler puts new players in a queue
		//so they should not be added before calling run()

		assertEquals(game.getPlayers().size(), 0);
	}

	@Test
	public void testAddPlayerAndRun() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().build());

		Player player = createPlayerMock(1L);
		game.addPlayer(player);
		game.run();

		assertEquals(game.getPlayers().size(), 1);
	}

	@Test
	public void testAddManyPlayers() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().withMaxPlayers(4).build());

		for(int i = 0; i < 4; i++) {
			Player player = createPlayerMock(i);
			game.addPlayer(player);
		}
		game.run();

		assertEquals(game.getPlayers().size(), 4);
	}

	@Test
	public void testAddTooManyPlayersBeforeRun() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().withMaxPlayers(1).build());

		Player player1 = createPlayerMock(1L);
		Player player2 = createPlayerMock(2L);
		game.addPlayer(player1);
		assertFalse(game.addPlayer(player2));
	}

	@Test
	public void testAddTooManyPlayersAfterRun() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().withMaxPlayers(1).build());

		Player player1 = createPlayerMock(1L);
		game.addPlayer(player1);
		game.run();

		Player player2 = createPlayerMock(2L);
		assertFalse(game.addPlayer(player2));
	}

	@Test
	public void testAddSamePlayerTwice() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().withMaxPlayers(1).build());

		Player player1 = createPlayerMock(1L);
		Player player2 = createPlayerMock(2L);
		game.addPlayer(player1);
		game.addPlayer(player2);
	}

	@Test
	public void testRemovePlayerAfterRun() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().withMaxPlayers(1).build());

		Player player1 = createPlayerMock(1L);
		game.addPlayer(player1);
		game.run();
		game.removePlayer(player1.getId());
		game.run();
		assertEquals(game.getPlayers().size(), 0);
	}

	@Test(expected = PlayerNotInGameException.class)
	public void testRemovePlayerBeforeRun() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().withMaxPlayers(1).build());

		Player player1 = createPlayerMock(1L);
		game.addPlayer(player1);
		game.removePlayer(player1.getId());
		assertEquals(game.getPlayers().size(), 0);
	}

	@Test(expected = PlayerAlreadyInGameException.class)
	public void testPlayerAlreadyExistsAfterRun() throws Exception {
		GamePlayerHandler game = getGamePlayerHandler(builder().withMaxPlayers(1).build());

		Player player1 = createPlayerMock(1L);
		game.addPlayer(player1);
		game.run();
		game.addPlayer(player1);
	}
}