package soze.multilife.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static spark.Spark.*;


/**
 * A very basic class which initializes a web server. It exposes client static files
 * and accepts a WebSocket handler for game communication.
 */
public class Server {

  private static final Logger LOG = LoggerFactory.getLogger(Server.class);

  private Server(
    int port,
    List<PathHandlerPair<Route>> pathHandlerPairs,
    List<PathHandlerPair<Object>> pathWebSocketHandlerPairs,
    List<String> staticFileHandlerPaths,
    List<String> externalStaticFileHandlerPaths
  ) {

    port(port);
    for (PathHandlerPair<Object> pair : pathWebSocketHandlerPairs) {
      webSocket(pair.getPath(), pair.getHandler());
    }
    for (String path : staticFileHandlerPaths) {
      staticFileLocation(path);
    }
    for (String path : externalStaticFileHandlerPaths) {
      try {
        staticFiles.externalLocation(path);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(5);
      }
    }
    for (PathHandlerPair<Route> pair : pathHandlerPairs) {
      get(pair.getPath(), pair.getHandler());
    }
  }

  /**
   * A simple builder for a server. Implemented to abstract the creation from the app
   * and to simplify creation.
   */
  public static class ServerBuilder {

    private final int port;
    private final List<PathHandlerPair<Route>> pathHandlerPairs = new ArrayList<>();
    private final List<PathHandlerPair<Object>> pathWebSocketHandlerPairs = new ArrayList<>();
    private final List<String> staticFileHandlerPaths = new ArrayList<>();
    private final List<String> externalStaticFileHandlerPaths = new ArrayList<>();

    public ServerBuilder(int port) {
      if (port < 0 || port > 65535) {
        throw new IllegalArgumentException("Port value is out of range (0-65535).");
      }
      this.port = port;
    }

    public ServerBuilder withWebSocketHandler(String path, Object handler) {
      pathWebSocketHandlerPairs.add(new PathHandlerPair<>(path, handler));
      return this;
    }

    public ServerBuilder withHttpHandler(String path, Route handler) {
      pathHandlerPairs.add(new PathHandlerPair<>(path, handler));
      return this;
    }

    public ServerBuilder withStaticFileHandler(String path) {
      staticFileHandlerPaths.add(Objects.requireNonNull(path));
      return this;
    }

    public ServerBuilder withExternalStaticFileHandler(String path) {
      externalStaticFileHandlerPaths.add(Objects.requireNonNull(path));
      return this;
    }

    public Server build() {
      return new Server(port, pathHandlerPairs, pathWebSocketHandlerPairs, staticFileHandlerPaths, externalStaticFileHandlerPaths);
    }

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
