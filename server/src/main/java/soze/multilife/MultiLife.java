package soze.multilife;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import soze.multilife.configuration.ConfigurationFactory;
import soze.multilife.configuration.MetricsConfigurationImpl;
import soze.multilife.configuration.MongoConfigurationImpl;
import soze.multilife.configuration.interfaces.ServerConfiguration;
import soze.multilife.events.EventBusHandler;
import soze.multilife.events.EventHandler;
import soze.multilife.helpers.UncaughtExceptionLogger;
import soze.multilife.metrics.MetricsHttpHandler;
import soze.multilife.metrics.MetricsService;
import soze.multilife.metrics.MetricsWebSocketHandler;
import soze.multilife.metrics.repository.MetricsRepository;
import soze.multilife.metrics.repository.MongoMetricsRepository;
import soze.multilife.server.GameSocketHandler;
import soze.multilife.server.Instance;
import soze.multilife.server.InstanceFactory;
import soze.multilife.server.Lobby;
import soze.multilife.server.Server.ServerBuilder;
import soze.multilife.server.connection.ConnectionFactory;
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

		ConfigurationFactory cfgFactory = new ConfigurationFactory();

		MultiLife ml = new MultiLife(cfgFactory);
		ml.start(cfgFactory.getServerConfiguration());

	}

	private final ConnectionFactory connectionFactory;
	private final Lobby lobby;
	private final MetricsWebSocketHandler metricsWebSocketHandler;
	private final MetricsHttpHandler metricsHttpHandler;
	private final EventHandler eventHandler;
	private final MetricsService metricsService;
	private MongoClient mongoClient;

	private MultiLife(ConfigurationFactory cfgFactory) {

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());

		SimulationFactory simulationFactory = new SimulationFactory(
			cfgFactory.getSimulationFactoryConfiguration()
		);

		Map<Long, Instance> instances = new HashMap<>();
		InstanceFactory instanceFactory = new InstanceFactory(
			instances,
			cfgFactory.getInstanceFactoryConfiguration(),
			simulationFactory
		);
		this.eventHandler = new EventBusHandler();
		this.lobby = new Lobby(eventHandler, instanceFactory, instances);

		this.connectionFactory = new ConnectionFactory(eventHandler);

		MetricsRepository metricsRepository = getMetricsRepository(cfgFactory.getMongoConfiguration());
		MetricsConfigurationImpl metricsConfiguration = cfgFactory.getMetricsConfiguration();
		this.metricsService = new MetricsService(
			metricsRepository,
			metricsConfiguration
		);
		this.eventHandler.register(metricsService);

		metricsHttpHandler = new MetricsHttpHandler();
		this.metricsWebSocketHandler = new MetricsWebSocketHandler(
			metricsConfiguration,
			metricsService, connectionFactory
		);

		Executor executor = Executors.newCachedThreadPool();
		executor.execute(lobby);
		executor.execute(metricsService);
		executor.execute(metricsWebSocketHandler);
	}

	private MetricsRepository getMetricsRepository(MongoConfigurationImpl config) {
		MongoCredential credential = MongoCredential.createCredential(
			config.getUsername(),
			config.getDatabase(),
			config.getPassword()
		);
		this.mongoClient = new MongoClient(
			new ServerAddress(
				config.getUrl(),
				config.getPort()
			),
			Arrays.asList(credential));

		return new MongoMetricsRepository(mongoClient.getDatabase(config.getDatabase()));
	}

	private void start(ServerConfiguration serverConfiguration) throws InterruptedException, ExecutionException {
		new ServerBuilder(serverConfiguration.getPort())
			.withWebSocketHandler("/game", new GameSocketHandler(lobby, connectionFactory))
			.withWebSocketHandler("/metrics-live", metricsWebSocketHandler)
			.withHttpHandler("/metrics", metricsHttpHandler)
			.withExternalStaticFileHandler(serverConfiguration.getExternalStaticFilesPath())
			.build();
	}

}
