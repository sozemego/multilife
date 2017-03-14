package soze.multilife;

import soze.multilife.events.EventBusHandler;
import soze.multilife.events.EventHandler;
import soze.multilife.helpers.UncaughtExceptionLogger;
import soze.multilife.server.GameSocketHandler;
import soze.multilife.server.Lobby;
import soze.multilife.server.Server;
import soze.multilife.server.connection.ConnectionFactory;
import soze.multilife.server.metrics.MetricsHttpHandler;
import soze.multilife.server.metrics.MetricsService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Entry point for the app.
 */
public class MultiLife {

  public static void main(String[] args) throws InterruptedException, ExecutionException {

	MultiLife ml = new MultiLife();
	ml.start();

  }

  private final ConnectionFactory connectionFactory;
  private final Lobby lobby;
  private final MetricsHttpHandler metricsHttpHandler;
  private final EventHandler eventHandler;

  private MultiLife() {
	Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
	this.eventHandler = new EventBusHandler();
	this.connectionFactory = new ConnectionFactory(eventHandler);
	this.lobby = new Lobby(eventHandler);
	MetricsService metricsService = new MetricsService();
	this.eventHandler.register(metricsService);
	metricsHttpHandler = new MetricsHttpHandler(metricsService);

	Executor executor = Executors.newCachedThreadPool();
	executor.execute(lobby);
	executor.execute(metricsService);
  }

  private void start() throws InterruptedException, ExecutionException {
	Server server = new Server.ServerBuilder(8080)
	  .withWebSocketHandler("/game", new GameSocketHandler(lobby, connectionFactory))
	  .withHttpHandler("/metrics", metricsHttpHandler)
	  .withStaticFileHandler("client")
	  .build();
	server.start();
  }

}
