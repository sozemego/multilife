package soze.multilife.game;

import org.junit.Test;

import static org.junit.Assert.*;

public class GameManagerDecoratorTest extends GameTest {

	@Test
	public void testRunnerEndsGameAfterCompletion() throws Exception {
		GameRunnerDecorator game = getGameRunner(builder().build());
		game.run();
		assertTrue(game.isScheduledForRemoval());
	}
}