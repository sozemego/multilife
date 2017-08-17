package soze.multilife.server;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.Test;
import org.mockito.Mockito;
import soze.multilife.messages.incoming.ClickMessage;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.connection.ConnectionFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GameSocketHandlerTest {

    @Test
    public void testOnConnect() throws Exception {
        Lobby lobby = Mockito.mock(Lobby.class);
        LoginService loginService = Mockito.mock(LoginService.class);
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        GameSocketHandler gameSocketHandler = new GameSocketHandler(lobby, loginService, connectionFactory);

        Session session = Mockito.mock(Session.class);
        gameSocketHandler.onOpen(session);
        verify(lobby, times(1)).onConnect(any(Connection.class));
    }

    @Test
    public void testOnDisconnect() throws Exception {
        Lobby lobby = Mockito.mock(Lobby.class);
        LoginService loginService = Mockito.mock(LoginService.class);
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        GameSocketHandler gameSocketHandler = new GameSocketHandler(lobby, loginService, connectionFactory);

        Session session = Mockito.mock(Session.class);
        gameSocketHandler.onOpen(session);
        gameSocketHandler.onClose(session, 400, "Because I can.");
        verify(lobby, times(1)).onDisconnect(any(Connection.class));
    }

    @Test
    public void testLoginMessageMessage() throws Exception {
        Lobby lobby = Mockito.mock(Lobby.class);
        LoginService loginService = Mockito.mock(LoginService.class);
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        GameSocketHandler gameSocketHandler = new GameSocketHandler(lobby, loginService, connectionFactory);

        Session session = Mockito.mock(Session.class);
        gameSocketHandler.onOpen(session);

        gameSocketHandler.onMessage(session, "{\"name\": \"Player\", \"type\":\"LOGIN\"}");
        verify(lobby, times(0)).onMessage(any(LoginMessage.class), eq(1));
    }

    @Test
    public void testOnClickMessage() throws Exception {
        Lobby lobby = Mockito.mock(Lobby.class);
        LoginService loginService = Mockito.mock(LoginService.class);
        ConnectionFactory connectionFactory = Mockito.mock(ConnectionFactory.class);
        GameSocketHandler gameSocketHandler = new GameSocketHandler(lobby, loginService, connectionFactory);

        Session session = Mockito.mock(Session.class);
        gameSocketHandler.onOpen(session);

        gameSocketHandler.onMessage(session, "{\"indices\": [1, 2, 3], \"type\":\"CLICK\"}");
        verify(lobby, times(1)).onMessage(any(ClickMessage.class), eq(1));
    }



}