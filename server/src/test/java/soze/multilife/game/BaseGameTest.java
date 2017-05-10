package soze.multilife.game;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.ClickMessage;

import java.util.Collection;

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
		assertTrue(game.getPlayers().containsKey(player1));
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

	@Test
	public void testAcceptMessage() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(1)
			.withInitialDensity(0f)
			.build();

		Player player1 = createPlayerMock(1L);
		game.addPlayer(player1);

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{0, 1, 2, 3, 4});

		game.acceptMessage(message, player1.getId());

		Collection<Cell> clickedCells = game.getClickedCells();
		assertEquals(clickedCells.size(), 5);
		assertTrue(clickedCells.contains(new Cell(0, 0)));
		assertTrue(clickedCells.contains(new Cell(0, 1)));
		assertTrue(clickedCells.contains(new Cell(0, 2)));
		assertTrue(clickedCells.contains(new Cell(0, 3)));
		assertTrue(clickedCells.contains(new Cell(0, 4)));
	}

	@Test(expected = PlayerNotInGameException.class)
	public void testAcceptMessagePlayerNotInGame() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(1)
			.withInitialDensity(0f)
			.build();

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{0, 1, 2, 3, 4});

		game.acceptMessage(message, 0L);
	}


}