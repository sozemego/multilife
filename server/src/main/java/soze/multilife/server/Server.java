package soze.multilife.server;

import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.HttpHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.StaticFileHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


/**
 * A very basic class which initializes a web server. It exposes client static files
 * and accepts a WebSocket handler for game communication.
 */
public class Server {

  private final WebServer webServer;

  private Server(
	int port,
	List<ServerBuilder.PathHandlerPair<HttpHandler>> pathHandlerPairs,
	List<ServerBuilder.PathHandlerPair<BaseWebSocketHandler>> pathWebSocketHandlerPairs,
	List<String> staticFileHandlerPaths) {
	this.webServer = WebServers.createWebServer(port);
	for (ServerBuilder.PathHandlerPair<HttpHandler> pair : pathHandlerPairs) {
	  webServer.add(pair.getPath(), pair.getHandler());
	}
	for (ServerBuilder.PathHandlerPair<BaseWebSocketHandler> pair : pathWebSocketHandlerPairs) {
	  webServer.add(pair.getPath(), pair.getHandler());
	}
	for (String path : staticFileHandlerPaths) {
	  webServer.add(new StaticFileHandler(path));
	}
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

  /**
   * A simple builder for a server. Implemented to abstract the creation from the app
   * and to simplify creation.
   */
  public static class ServerBuilder {

	private final int port;
	private final List<PathHandlerPair<HttpHandler>> pathHandlerPairs = new ArrayList<>();
	private final List<PathHandlerPair<BaseWebSocketHandler>> pathWebSocketHandlerPairs = new ArrayList<>();
	private final List<String> staticFileHandlerPaths = new ArrayList<>();

	public ServerBuilder(int port) {
	  if (port < 0 || port > 65535) {
		throw new IllegalArgumentException("Port value is out of range (0-65535).");
	  }
	  this.port = port;
	}

	public ServerBuilder withWebSocketHandler(String path, BaseWebSocketHandler handler) {
	  pathWebSocketHandlerPairs.add(new PathHandlerPair<>(path, handler));
	  return this;
	}

	public ServerBuilder withHttpHandler(String path, HttpHandler handler) {
	  pathHandlerPairs.add(new PathHandlerPair<>(path, handler));
	  return this;
	}

	public ServerBuilder withStaticFileHandler(String path) {
	  staticFileHandlerPaths.add(Objects.requireNonNull(path));
	  return this;
	}

	public Server build() {
	  return new Server(port, pathHandlerPairs, pathWebSocketHandlerPairs, staticFileHandlerPaths);
	}

	private static class PathHandlerPair<T> {

	  private final String path;
	  private final T handler;

	  PathHandlerPair(String path, T handler) {
		this.path = Objects.requireNonNull(path);
		this.handler = Objects.requireNonNull(handler);
	  }

	  String getPath() {
		return path;
	  }

	  T getHandler() {
		return handler;
	  }
	}

  }

}
