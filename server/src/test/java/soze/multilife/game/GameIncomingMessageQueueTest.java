package soze.multilife.game;

import org.junit.Test;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.ClickMessage;

import java.awt.*;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;


public class GameIncomingMessageQueueTest extends GameTest {

	@Test
	public void testAcceptMessage() throws Exception {
		GameIncomingMessageQueue game = getGameIncomingMessageQueue(builder().build());

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{1, 2, 3});

		Player player = createPlayerMock(1L);
		game.addPlayer(player);
		// This decorator queues incoming messages
		// so they should not have effects immidiately
		game.acceptMessage(message, 1L);
		Collection<Cell> clickedCells = game.getClickedCells();
		assertTrue(clickedCells.isEmpty());
	}

	@Test
	public void testAcceptMessageAndRun() throws Exception {
		GameIncomingMessageQueue game = getGameIncomingMessageQueue(builder().build());

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{1, 2, 3});

		Player player = createPlayerMock(1L);
		game.addPlayer(player);
		game.acceptMessage(message, 1L);
		game.run();
		// running the game clears clicked cells after handling the message
		// so it should still be empty
		Collection<Cell> clickedCells = game.getClickedCells();
		assertEquals(clickedCells.size(), 0);

		Map<Point, Cell> cells = game.getAllCells();
		assertTrue(cells.get(new Point(1, 0)).isAlive());
		assertTrue(cells.get(new Point(2, 0)).isAlive());
		assertTrue(cells.get(new Point(3, 0)).isAlive());
	}

	@Test(expected = PlayerNotInGameException.class)
	public void testAcceptMessagePlayerNotInGame() throws Exception {
		GameIncomingMessageQueue game = getGameIncomingMessageQueue(builder().build());

		ClickMessage message = new ClickMessage();
		message.setIndices(new int[]{1, 2, 3});

		game.acceptMessage(message, 1L);
	}

}