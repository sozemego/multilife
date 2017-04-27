package soze.multilife;

import soze.multilife.events.EventBusHandler;
import soze.multilife.events.EventHandler;
import soze.multilife.helpers.UncaughtExceptionLogger;
import soze.multilife.server.GameSocketHandler;
import soze.multilife.server.Instance;
import soze.multilife.server.InstanceFactory;
import soze.multilife.server.Lobby;
import soze.multilife.server.Server.ServerBuilder;
import soze.multilife.server.connection.ConnectionFactory;
import soze.multilife.server.metrics.MetricsHttpHandler;
import soze.multilife.server.metrics.MetricsService;
import soze.multilife.server.metrics.MetricsWebSocketHandler;
import soze.multilife.simulation.SimulationFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Entry point for the app.
 */
public class MultiLife {

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		Configuration config = new Configuration();
		config.load();

		MultiLife ml = new MultiLife(config);
		ml.start();

	}

	private final ConnectionFactory connectionFactory;
	private final Lobby lobby;
	private final MetricsWebSocketHandler metricsWebSocketHandler;
	private final MetricsHttpHandler metricsHttpHandler;
	private final EventHandler eventHandler;
	private final MetricsService metricsService;

	private MultiLife(Configuration config) {

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());
		this.eventHandler = new EventBusHandler();

		SimulationFactory simulationFactory = new SimulationFactory(
			config.getIntSupplier("gameDefaultWidth"),
			config.getIntSupplier("gameDefaultHeight")
		);

		Map<Long, Instance> instances = new HashMap<>();
		InstanceFactory instanceFactory = new InstanceFactory(
			instances,
			config.getIntSupplier("maxPlayersPerInstance"),
			config.getLongSupplier("gameDuration"),
			config.getLongSupplier("gameIterationInterval"),
			config.getLongSupplier("instanceInactiveTimeBeforeRemoval"),
			simulationFactory
		);
		this.lobby = new Lobby(eventHandler, instanceFactory, instances);

		this.connectionFactory = new ConnectionFactory(eventHandler);

		this.metricsService = new MetricsService(config.getLongSupplier("calculateMetricsInterval"));
		this.eventHandler.register(metricsService);
		metricsHttpHandler = new MetricsHttpHandler();
		this.metricsWebSocketHandler = new MetricsWebSocketHandler(
			config.getLongSupplier("metricsPushUpdateRate"),
			metricsService, connectionFactory
		);

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
			.withExternalStaticFileHandler("f:/multilife/server/src/main/resources/public")
			.build();
	}

}
