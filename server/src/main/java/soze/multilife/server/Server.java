package soze.multilife.server;

import org.webbitserver.*;
import org.webbitserver.handler.StaticFileHandler;

import java.util.Objects;
import java.util.concurrent.ExecutionException;


/**
 * A very basic class which initializes a web server. It exposes client static files
 * and accepts a WebSocket handler for game communication.
 */
public class Server {

	private final WebServer webServer;

	/**
	 * Creates a new Server. It creates a new server at a given port.
	 * gameSocketHandler cannot be null. It's an object which will handle game connections.
	 * @param port
	 * @param gameSocketHandler
	 */
	public Server(int port, BaseWebSocketHandler gameSocketHandler) {
		this.webServer = WebServers.createWebServer(port)
				.add("/game", Objects.requireNonNull(gameSocketHandler))
				.add(new StaticFileHandler("client"));
		System.out.println("Server is listening on: " + webServer.getUri());
	}

	/**
	 * Starts the server. This call is blocking until the server is ready to accept incoming requests.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void start() throws InterruptedException, ExecutionException {
		this.webServer.start().get();
	}

}
