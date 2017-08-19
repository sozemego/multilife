package soze.multilife.game;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.ClickMessage;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.messages.outgoing.PlayerRemoved;

import java.awt.*;
import java.util.Collection;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BaseGameTest extends GameTest {

	@Before
	public void setUp() {

	}

	@Test
	public void testAddPlayer() throws Exception {
		BaseGame game = createGameWithDefaultValues();
		Player player = createPlayerMock(1);
		boolean result = game.addPlayer(player);
		assertTrue(result);
		assertEquals(game.getPlayers().size(), 1);
	}

	@Test
	public void testAddPlayersTooMany() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(2)
			.build();

		Player player1 = createPlayerMock(1);

		Player player2 = createPlayerMock(2);

		Player player3 = createPlayerMock(3);

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

		Player player1 = createPlayerMock(1);
		Player player2 = createPlayerMock(2);

		game.addPlayer(player1);
		game.addPlayer(player2);

		game.removePlayer(player2.getId());
		assertEquals(game.getPlayers().size(), 1);
		assertTrue(game.getPlayers().containsKey(player1.getId()));
		assertFalse(game.getPlayers().containsKey(player2.getId()));
	}

	@Test
	public void testIsFull() throws Exception {
		BaseGame game = builder()
				.withMaxPlayers(2)
				.build();
		Player player1 = createPlayerMock(1);
		Player player2 = createPlayerMock(2);

		game.addPlayer(player1);
		game.addPlayer(player2);
		assertTrue(game.isFull());
	}

	@Test
	public void testEndGame() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(2)
			.build();

		Player player1 = createPlayerMock(1);
		Player player2 = createPlayerMock(2);

		game.addPlayer(player1);
		game.addPlayer(player2);

		game.end();
		assertTrue(game.isScheduledForRemoval());
		Mockito.verify(player1, times(1)).disconnect();
		Mockito.verify(player2, times(1)).disconnect();
	}

	@Test
	public void testAcceptMessage() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(1)
			.withInitialDensity(0f)
			.build();

		Player player1 = createPlayerMock(1);
		game.addPlayer(player1);

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{0, 1, 2, 3, 4});

		game.acceptMessage(message, player1.getId());

		Collection<Cell> clickedCells = game.getClickedCells();
		assertEquals(clickedCells.size(), 5);
		assertTrue(clickedCells.contains(new Cell(0, 0)));
		assertTrue(clickedCells.contains(new Cell(1, 0)));
		assertTrue(clickedCells.contains(new Cell(2, 0)));
		assertTrue(clickedCells.contains(new Cell(3, 0)));
		assertTrue(clickedCells.contains(new Cell(4, 0)));
	}

	@Test(expected = PlayerNotInGameException.class)
	public void testAcceptMessagePlayerNotInGame() throws Exception {
		BaseGame game = builder()
			.withMaxPlayers(1)
			.withInitialDensity(0f)
			.build();

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{0, 1, 2, 3, 4});

		game.acceptMessage(message, 0);
	}

	@Test
	public void testPlayerClicksTwice() throws Exception {
		BaseGame game = builder()
				.withMaxPlayers(1)
				.withInitialDensity(0f)
				.build();

		Player player1 = createPlayerMock(1);
		game.addPlayer(player1);

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{0, 1, 2, 3, 4});
		game.acceptMessage(message, player1.getId());

		ClickMessage secondMessage = new ClickMessage();
		secondMessage.setIndices(new int[]{5, 12, 56, 5});
		game.acceptMessage(message, player1.getId());

		Collection<Cell> clickedCells = game.getClickedCells();
		assertEquals(clickedCells.size(), 5);
	}

	@Test
	public void testClickedOnAlreadyAliveCells() throws Exception {
		BaseGame game = builder()
				.withMaxPlayers(2)
				.withInitialDensity(0f)
				.build();

		Player player1 = createPlayerMock(1);
		game.addPlayer(player1);

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{0, 1, 2, 3, 4});
		game.acceptMessage(message, player1.getId());

		Player player2 = createPlayerMock(2);
		game.addPlayer(player2);

		ClickMessage anotherMessage = new ClickMessage();
		anotherMessage.setIndices(new int[]{4, 12, 16, 20, 42});
		game.acceptMessage(message, player2.getId());

		Collection<Cell> clickedCells = game.getClickedCells();
		assertEquals(clickedCells.size(), 5);
	}

	@Test
	public void testProperInitialization() throws Exception {
		int width = 25;
		int height = 25;
		BaseGame game = builder()
				.withWidth(width)
				.withHeight(height)
				.withInitialDensity(0.5f)
				.build();

		Map<Point, Cell> cells = game.getAllCells();
		assertEquals(cells.size(), 25 * 25);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidWidthHeight() throws Exception {
		int width = 0;
		int height = 25;
		builder().withWidth(width)
				.withHeight(height)
				.build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidWidthHeight2() throws Exception {
		int width = -25;
		int height = 0;
		builder().withWidth(width)
				.withHeight(height)
				.build();
	}

	@Test
	public void testRemainingTime() throws Exception {
		BaseGame game = builder().withDuration(250).build();
		assertEquals(game.getRemainingTime(), 250);
	}

	@Test
	public void testIsOutOfTime() throws Exception {
		BaseGame game = builder().withDuration(0).build();
		assertEquals(game.getRemainingTime(), 0);
		assertTrue(game.isOutOfTime());
	}

	@Test
	public void testDoesNotAdvanceWithoutPlayers() throws Exception {
		BaseGame game = builder().withMaxPlayers(1).build();
		game.run();
		game.run();
		game.run();
		assertEquals(game.getIterations(), 0);
	}

	@Test
	public void testSendMessage() throws Exception {
		BaseGame game = builder()
				.withMaxPlayers(2)
				.build();

		Player player1 = createPlayerMock(1);
		Player player2 = createPlayerMock(2);

		game.addPlayer(player1);
		game.addPlayer(player2);

		PlayerRemoved data = new PlayerRemoved(1);
		game.sendMessage(data);
		verify(player1, times(1)).send(any(OutgoingMessage.class));
		verify(player2, times(1)).send(any(OutgoingMessage.class));
	}

	@Test
	public void testRunWithPlayers() throws Exception {
		BaseGame game = builder()
				.withMaxPlayers(2)
				.build();

		Player player1 = createPlayerMock(1);
		Player player2 = createPlayerMock(2);

		game.addPlayer(player1);
		game.addPlayer(player2);

		game.run();
		game.run();

		assertEquals(game.getIterations(), 2);
	}

	@Test
	public void testScheduledForRemoval() throws Exception {
		BaseGame game = builder()
				.withMaxPlayers(2)
				.withDuration(0)
				.build();

		game.run();

		assertEquals(game.isScheduledForRemoval(), true);
	}

	@Test
	public void testGetOnePlayer() throws Exception {
		BaseGame game = builder().build();

		Player player1 = createPlayerMock(1);
		game.addPlayer(player1);

		String playerColor = game.getPlayerColor(player1.getId());
		assertTrue(playerColor != null);
	}

}