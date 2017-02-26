package soze.multilife.server;

import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;
import soze.multilife.server.metrics.MetricsHttpHandler;

import java.util.Objects;
import java.util.concurrent.ExecutionException;


/**
 * A very basic class which initializes a web server. It exposes client static files
 * and accepts a WebSocket handler for game communication.
 */
public class Server {

  private final WebServer webServer;

  /**
   * Creates a new Server at a given port.
   *
   * @param port
   */
  public Server(int port, GameSocketHandler gameSocketHandler, MetricsHttpHandler metricsHttpHandler) {
	this.webServer = WebServers.createWebServer(port)
	  .add("/game", Objects.requireNonNull(gameSocketHandler))
	  .add("/metrics", Objects.requireNonNull(metricsHttpHandler))
	  .add(new StaticFileHandler("client"));
  }

  /**
   * Starts the server. This call is blocking until the server is ready to accept incoming requests.
   *
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public void start() throws InterruptedException, ExecutionException {
	webServer.start().get();
	System.out.println("Server is listening on: " + webServer.getUri());
  }

}
