package soze.multilife;

import soze.multilife.events.EventBusHandler;
import soze.multilife.events.EventHandler;
import soze.multilife.helpers.UncaughtExceptionLogger;
import soze.multilife.server.GameSocketHandler;
import soze.multilife.server.Lobby;
import soze.multilife.server.Server.ServerBuilder;
import soze.multilife.server.connection.ConnectionFactory;
import soze.multilife.server.metrics.MetricsHttpHandler;
import soze.multilife.server.metrics.MetricsService;
import soze.multilife.server.metrics.MetricsWebSocketHandler;

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
  private final MetricsWebSocketHandler metricsWebSocketHandler;
  private final MetricsHttpHandler metricsHttpHandler;
  private final EventHandler eventHandler;
  private final MetricsService metricsService;

  private MultiLife() {
	Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
	this.eventHandler = new EventBusHandler();
	this.connectionFactory = new ConnectionFactory(eventHandler);
	this.lobby = new Lobby(eventHandler);
	this.metricsService = new MetricsService();
	this.eventHandler.register(metricsService);
	metricsHttpHandler = new MetricsHttpHandler(metricsService);
	this.metricsWebSocketHandler = new MetricsWebSocketHandler(metricsService, connectionFactory);

	Executor executor = Executors.newCachedThreadPool();
	executor.execute(lobby);
	executor.execute(metricsService);
	executor.execute(metricsWebSocketHandler);
  }

  private void start() throws InterruptedException, ExecutionException {
	new ServerBuilder(8080)
	  .withWebSocketHandler("/game", new GameSocketHandler(lobby, connectionFactory))
	  .withWebSocketHandler("/metrics-live", metricsWebSocketHandler)
	  .withHttpHandler("/metrics", metricsHttpHandler)
	  .withStaticFileHandler("/public")
	  .build();
  }

}
