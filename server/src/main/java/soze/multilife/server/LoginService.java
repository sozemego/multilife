package soze.multilife.server;

import soze.multilife.game.Player;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.server.connection.Connection;

/**
 * Class responsible for logging players to the game.
 * Right now, all this class does is create Player objects, but can be extended in the future.
 */
class LoginService {

	Player login(LoginMessage message, Connection connection) {
		return new Player(connection.getId(), connection, message.getName(), "BASIC");
	}

}
