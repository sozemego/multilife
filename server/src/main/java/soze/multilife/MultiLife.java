package soze.multilife;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.configuration.ConfigurationFactory;
import soze.multilife.configuration.interfaces.MetricsConfiguration;
import soze.multilife.configuration.interfaces.MongoConfiguration;
import soze.multilife.configuration.interfaces.ServerConfiguration;
import soze.multilife.events.EventBus;
import soze.multilife.events.EventBusImpl;
import soze.multilife.game.GameFactory;
import soze.multilife.metrics.MetricsHttpHandler;
import soze.multilife.metrics.MetricsWebSocketHandler;
import soze.multilife.metrics.MetricsWebSocketHandlerImpl;
import soze.multilife.metrics.NullMetricsSocketHandler;
import soze.multilife.metrics.repository.MetricsRepository;
import soze.multilife.metrics.repository.MongoMetricsRepository;
import soze.multilife.metrics.service.MetricsServiceImpl;
import soze.multilife.metrics.service.NullMetricsService;
import soze.multilife.server.GameSocketHandler;
import soze.multilife.server.Lobby;
import soze.multilife.server.LoginService;
import soze.multilife.server.Server.ServerBuilder;
import soze.multilife.server.connection.ConnectionFactory;
import soze.multilife.server.gamerunner.GameManager;
import soze.multilife.utils.UncaughtExceptionLogger;
import spark.Route;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Entry point for the app.
 */
public class MultiLife {

  private static final Logger LOG = LoggerFactory.getLogger(MultiLife.class);

  public static void main(String[] args) throws InterruptedException, ExecutionException {

    ConfigurationFactory cfgFactory = new ConfigurationFactory();

    MultiLife ml = new MultiLife(cfgFactory);
    ml.start(cfgFactory.getServerConfiguration());

  }

  private final Executor executor = Executors.newCachedThreadPool();
  private final ConnectionFactory connectionFactory;
  private final LoginService loginService;
  private final Lobby lobby;
  private final EventBus eventBus;
  private final MetricsWebSocketHandler metricsWebSocketHandler;
  private final MetricsHttpHandler metricsHttpHandler;

  private MultiLife(ConfigurationFactory cfgFactory) {

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionLogger());

    this.loginService = new LoginService();

    this.eventBus = new EventBusImpl();
    GameManager gameManager = new GameManager(cfgFactory.getGameRunnerConfiguration());
    GameFactory gameFactory = new GameFactory(
      cfgFactory.getGameConfiguration()
    );
    this.lobby = new Lobby(eventBus, gameManager, gameFactory);

    this.connectionFactory = new ConnectionFactory(eventBus);

    MetricsConfiguration metricsConfiguration = cfgFactory.getMetricsConfiguration();

    if (metricsConfiguration.isMetricsEnabled()) {

      MongoConfiguration mongoConfiguration = cfgFactory.getMongoConfiguration();
      MongoClient mongoClient = createMongoClient(mongoConfiguration);

      MetricsRepository metricsRepository =
        new MongoMetricsRepository(
          mongoClient.getDatabase(mongoConfiguration.getDatabase())
        );

      MetricsServiceImpl metricsService = new MetricsServiceImpl(
        metricsRepository,
        metricsConfiguration
      );
      this.eventBus.register(metricsService);

      metricsHttpHandler = new MetricsHttpHandler(metricsService);
      this.metricsWebSocketHandler = new MetricsWebSocketHandlerImpl(
        metricsConfiguration,
        metricsService, connectionFactory
      );

      executor.execute(metricsService);
      executor.execute(metricsWebSocketHandler);
    } else {
      this.metricsHttpHandler = new MetricsHttpHandler(new NullMetricsService()) {
        public Route getMetricsRoute() {
          return (req, res) -> "Metrics disabled";
        }

        public Route getMetricsOutgoingApi() {
          return (request, response) -> "Metrics disabled";
        }
      };
      this.metricsWebSocketHandler = new NullMetricsSocketHandler();
    }
    LOG.info("Metrics are " + (metricsConfiguration.isMetricsEnabled() ? "enabled" : "disabled") + ".");

    executor.execute(lobby);
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
      Collections.singletonList(credential)
    );
  }

  private void start(ServerConfiguration serverConfiguration) throws InterruptedException, ExecutionException {
    new ServerBuilder(serverConfiguration.getServerPort())
      .withWebSocketHandler("/game", new GameSocketHandler(lobby, loginService, connectionFactory, eventBus))
      .withWebSocketHandler("/metrics-live", metricsWebSocketHandler)
      .withHttpHandler("/metrics", metricsHttpHandler.getMetricsRoute())
      .withHttpHandler("/metrics/outgoing", metricsHttpHandler.getMetricsOutgoingApi())
      .withHttpHandler("/metrics/incoming", metricsHttpHandler.getMetricsIncomingApi())
      .withExternalStaticFileHandler(serverConfiguration.getExternalStaticFilesPath())
      .build();
  }

}
