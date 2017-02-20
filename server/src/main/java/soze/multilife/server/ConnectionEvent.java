package soze.multilife.server;

import java.net.Socket;

/**
 * Created by soze on 2/20/2017.
 */
public class ConnectionEvent {

	private final Socket socket;

	public ConnectionEvent(Socket socket) {
		this.socket = socket;
	}

	public Socket getSocket() {
		return socket;
	}

}
