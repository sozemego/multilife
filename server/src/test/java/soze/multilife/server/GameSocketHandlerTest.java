package soze.multilife.server;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import soze.multilife.events.EventBus;
import soze.multilife.messages.incoming.ClickMessage;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.connection.ConnectionFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GameSocketHandlerTest {

	@Mock
	private Lobby lobby;

	@Mock
	private LoginService loginService;

	@Mock
	private ConnectionFactory connectionFactory;

	@Mock
	private EventBus bus;

    @Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

    @Test
    public void testOnConnect() throws Exception {
        GameSocketHandler gameSocketHandler = new GameSocketHandler(lobby, loginService, connectionFactory, bus);

        Session session = Mockito.mock(Session.class);
        gameSocketHandler.onOpen(session);
        verify(lobby, times(1)).onConnect(any(Connection.class));
    }

    @Test
    public void testOnDisconnect() throws Exception {
		GameSocketHandler gameSocketHandler = new GameSocketHandler(lobby, loginService, connectionFactory, bus);

        Session session = Mockito.mock(Session.class);
        gameSocketHandler.onOpen(session);
        gameSocketHandler.onClose(session, 400, "Because I can.");
        verify(lobby, times(1)).onDisconnect(any(Connection.class));
    }

    @Test
    public void testLoginMessageMessage() throws Exception {
		GameSocketHandler gameSocketHandler = new GameSocketHandler(lobby, loginService, connectionFactory, bus);

        Session session = Mockito.mock(Session.class);
        gameSocketHandler.onOpen(session);

        gameSocketHandler.onMessage(session, "{\"name\": \"Player\", \"type\":\"LOGIN\"}");
        verify(lobby, times(0)).onMessage(any(LoginMessage.class), eq(1));
    }

    @Test
    public void testOnClickMessage() throws Exception {
		GameSocketHandler gameSocketHandler = new GameSocketHandler(lobby, loginService, connectionFactory, bus);

        Session session = Mockito.mock(Session.class);
        gameSocketHandler.onOpen(session);

        gameSocketHandler.onMessage(session, "{\"indices\": [1, 2, 3], \"type\":\"CLICK\"}");
        verify(lobby, times(1)).onMessage(any(ClickMessage.class), eq(1));
    }

}