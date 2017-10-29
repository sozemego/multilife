package soze.multilife.server;

import soze.multilife.game.Player;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.server.connection.Connection;

import java.util.Objects;

/**
 * Class responsible for logging players to the game.
 * Right now, all this class does is create Player objects, but can be extended in the future.
 */
public class LoginService {

  public Player login(LoginMessage message, Connection connection) {
    Objects.requireNonNull(message);
    Objects.requireNonNull(connection);
    return new Player(connection.getId(), connection, message.getName());
  }

}
