package soze.multilife.server;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import soze.multilife.game.Player;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.server.connection.Connection;

import static org.junit.Assert.assertTrue;

public class LoginServiceTest {

	private LoginService loginService;

	@Before
	public void setUp() {
		loginService = new LoginService();
	}

	@Test
	public void testLogin() throws Exception {
		LoginMessage loginMessage = new LoginMessage();
		loginMessage.setName("Player");

		Connection connection = Mockito.mock(Connection.class);
		Mockito.when(connection.getId()).thenReturn(1);

		Player player = loginService.login(loginMessage, connection);

		assertTrue(player.getName().equals("Player"));
		assertTrue(player.getId() == 1L);
	}

	@Test(expected = NullPointerException.class)
	public void testInvalidParametersLoginMessage() throws Exception {
		Connection connection = Mockito.mock(Connection.class);
		Mockito.when(connection.getId()).thenReturn(1);
		loginService.login(null, connection);
	}

	@Test(expected = NullPointerException.class)
	public void testInvalidParametersConnection() throws Exception {
		LoginMessage loginMessage = new LoginMessage();
		loginMessage.setName("Player");
		loginService.login(loginMessage, null);
	}

}