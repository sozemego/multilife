package soze.multilife.game;

import org.junit.Test;
import soze.multilife.messages.outgoing.OutgoingMessage;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GameOutgoingMessageHandlerTest extends GameTest {

	@Test
	public void testSendingMessages() throws Exception {
		GameOutgoingMessageHandler game = getGameOutgoingMessageHandler(builder().build());

		Player player = createPlayerMock(1L);
		game.addPlayer(player);
		game.run();

		verify(player, times(4)).send(any(OutgoingMessage.class));
	}

}