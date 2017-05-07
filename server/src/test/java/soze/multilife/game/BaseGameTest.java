package soze.multilife.game;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseGameTest extends GameTest {

	@Before
	public void setUp() {

	}

	@Test
	public void testAddPlayer() throws Exception {
		BaseGame game = createGameWithDefaultValues();
		Player player = createPlayerMock(1L);
		boolean result = game.addPlayer(player);
		assertTrue(result);
		assertEquals(game.getPlayers().size(), 1);
	}

	@Test
	public void testAddPlayersTooMany() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(2)
			.build();

		Player player1 = createPlayerMock(1L);

		Player player2 = createPlayerMock(2L);

		Player player3 = createPlayerMock(3L);

		boolean result = game.addPlayer(player1);
		assertTrue(result);
		assertEquals(game.getPlayers().size(), 1);

		result = game.addPlayer(player2);
		assertTrue(result);
		assertEquals(game.getPlayers().size(), 2);

		result = game.addPlayer(player3);
		assertFalse(result);
		assertEquals(game.getPlayers().size(), 2);
	}

	@Test
	public void testRemovePlayer() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(2)
			.build();

		Player player1 = createPlayerMock(1L);
		Player player2 = createPlayerMock(2L);

		game.addPlayer(player1);
		game.addPlayer(player2);

		game.removePlayer(player2.getId());
		assertEquals(game.getPlayers().size(), 1);
		assertTrue(game.getPlayers().contains(player1));
	}

	@Test
	public void testEndGame() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(2)
			.build();

		Player player1 = createPlayerMock(1L);
		Player player2 = createPlayerMock(2L);

		game.addPlayer(player1);
		game.addPlayer(player2);

		game.end();
		assertTrue(game.isScheduledForRemoval());
		Mockito.verify(player1, Mockito.times(1)).disconnect();
		Mockito.verify(player2, Mockito.times(1)).disconnect();

	}


}