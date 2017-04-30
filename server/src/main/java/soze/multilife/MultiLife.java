package soze.multilife;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
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
import soze.multilife.server.metrics.repository.MetricsRepository;
import soze.multilife.server.metrics.repository.MongoMetricsRepository;
import soze.multilife.simulation.SimulationFactory;

import java.util.Arrays;
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
	private final MongoClient mongoClient;

	private MultiLife(Configuration config) {

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());

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
		this.eventHandler = new EventBusHandler();
		this.lobby = new Lobby(eventHandler, instanceFactory, instances);

		this.connectionFactory = new ConnectionFactory(eventHandler);

		MongoCredential credential = MongoCredential.createCredential(
			config.getStringSupplier("mongoUsername").get(),
			config.getStringSupplier("mongoDatabase").get(),
			config.getStringSupplier("mongoPassword").get().toCharArray()
		);
		this.mongoClient = new MongoClient(
			new ServerAddress(
				config.getStringSupplier("mongoHost").get(),
				config.getIntSupplier("mongoPort").get()
			),
			Arrays.asList(credential));

		MetricsRepository metricsRepository = new MongoMetricsRepository(mongoClient.getDatabase(config.getStringSupplier("mongoDatabase").get()));
		this.metricsService = new MetricsService(
			metricsRepository,
			config.getLongSupplier("calculateMetricsInterval"),
			config.getLongSupplier("metricsKbsIntervalBetweenSaves")
		);
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
