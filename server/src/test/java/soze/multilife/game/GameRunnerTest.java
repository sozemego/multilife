package soze.multilife.game;

import org.junit.Test;

import static org.junit.Assert.*;

public class GameRunnerTest extends GameTest {

	@Test
	public void testRunnerEndsGameAfterCompletion() throws Exception {
		GameRunner game = getGameRunner(builder().build());
		game.run();
		assertTrue(game.isScheduledForRemoval());
	}
}