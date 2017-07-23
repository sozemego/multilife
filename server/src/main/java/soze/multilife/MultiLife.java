package soze.multilife;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import soze.multilife.configuration.ConfigurationFactory;
import soze.multilife.configuration.interfaces.MetricsConfiguration;
import soze.multilife.configuration.interfaces.MongoConfiguration;
import soze.multilife.configuration.interfaces.ServerConfiguration;
import soze.multilife.events.EventBus;
import soze.multilife.events.EventBusImpl;
import soze.multilife.game.GameFactory;
import soze.multilife.utils.UncaughtExceptionLogger;
import soze.multilife.metrics.MetricsHttpHandler;
import soze.multilife.metrics.MetricsService;
import soze.multilife.metrics.MetricsWebSocketHandler;
import soze.multilife.metrics.repository.MetricsRepository;
import soze.multilife.metrics.repository.MongoMetricsRepository;
import soze.multilife.server.GameSocketHandler;
import soze.multilife.server.Lobby;
import soze.multilife.server.LoginService;
import soze.multilife.server.Server.ServerBuilder;
import soze.multilife.server.connection.ConnectionFactory;

import java.util.Arrays;
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
	private final LoginService loginService;
	private final Lobby lobby;
	private final MetricsWebSocketHandler metricsWebSocketHandler;
	private final MetricsHttpHandler metricsHttpHandler;
	private final EventBus eventBus;
	private final MetricsService metricsService;
	private final MongoClient mongoClient;

	private MultiLife(ConfigurationFactory cfgFactory) {

		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());

		GameFactory gameFactory = new GameFactory(
			cfgFactory.getGameConfiguration()
		);

		this.loginService = new LoginService();

		this.eventBus = new EventBusImpl();
		this.lobby = new Lobby(eventBus, gameFactory);

		this.connectionFactory = new ConnectionFactory(eventBus);

		this.mongoClient = createMongoClient(cfgFactory.getMongoConfiguration());

		MetricsRepository metricsRepository = new MongoMetricsRepository(mongoClient.getDatabase(cfgFactory.getMongoConfiguration().getDatabase()));
		MetricsConfiguration metricsConfiguration = cfgFactory.getMetricsConfiguration();
		this.metricsService = new MetricsService(
			metricsRepository,
			metricsConfiguration
		);
		this.eventBus.register(metricsService);

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

	private MongoClient createMongoClient(MongoConfiguration config) {
		MongoCredential credential = MongoCredential.createCredential(
			config.getUsername(),
			config.getDatabase(),
			config.getPassword()
		);
		return new MongoClient(
			new ServerAddress(
				config.getHost(),
				config.getDatabasePort()
			),
			Arrays.asList(credential));
	}

	private void start(ServerConfiguration serverConfiguration) throws InterruptedException, ExecutionException {
		new ServerBuilder(serverConfiguration.getServerPort())
			.withWebSocketHandler("/game", new GameSocketHandler(lobby, loginService, connectionFactory))
			.withWebSocketHandler("/metrics-live", metricsWebSocketHandler)
			.withHttpHandler("/metrics", metricsHttpHandler)
			.withExternalStaticFileHandler(serverConfiguration.getExternalStaticFilesPath())
			.build();
	}

}
