package soze.multilife.configuration;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import soze.multilife.configuration.interfaces.*;

import java.util.Objects;

public class Configuration
  implements GameConfiguration, MetricsConfiguration, MongoConfiguration, ServerConfiguration,
  GameRunnerConfiguration {

  private static final String GAME_CONFIGURATION = "GAME_CONFIGURATION";
  private static final String GAME_DEFAULT_INITIAL_DENSITY = "gameDefaultInitialDensity";
  private static final String GAME_DURATION = "gameDuration";
  private static final String GAME_MAX_PLAYERS_PER_GAME = "gameMaxPlayersPerGame";
  private static final String GAME_DEFAULT_WIDTH = "gameDefaultWidth";
  private static final String GAME_DEFAULT_HEIGHT = "gameDefaultHeight";
  private static final String GAME_ITERATION_INTERVAL = "gameIterationInterval";

  private static final String METRICS_CONFIGURATION = "METRICS_CONFIGURATION";
  private static final String CALCULATE_METRICS_INTERVAL = "calculateMetricsInterval";
  private static final String METRICS_ENABLED = "metricsEnabled";
  private static final String METRICS_PUSH_UPDATE_RATE = "metricsPushUpdateRate";
  private static final String METRICS_INTERVAL_BETWEEN_SAVES = "metricsIntervalBetweenSaves";

  private static final String MONGO_CONFIGURATION = "MONGO_CONFIGURATION";
  private static final String MONGO_USERNAME = "mongoUsername";
  private static final String MONGO_PASSWORD = "mongoPassword";
  private static final String MONGO_DATABASE = "mongoDatabase";
  private static final String MONGO_HOST = "mongoHost";
  private static final String MONGO_PORT = "mongoPort";

  private static final String SERVER_CONFIGURATION = "SERVER_CONFIGURATION";
  private static final String SERVER_PORT = "serverPort";
  private static final String EXTERNAL_STATIC_FILES_PATH = "externalStaticFilesPath";

  private static final String GAME_RUNNER_CONFIGURATION = "GAME_RUNNER_CONFIGURATION";
  private static final String GAMES_PER_THREAD = "gamesPerThread";

  private static final Multimap<String, String> ALL_DEFAULT_PROPERTIES;

  static {
    Multimap<String, String> defaultProperties = ArrayListMultimap.create();
    defaultProperties.put(GAME_CONFIGURATION, GAME_DEFAULT_INITIAL_DENSITY + " = " + 0);
    defaultProperties.put(GAME_CONFIGURATION, GAME_DURATION + " = " + (1000 * 60 * 5));
    defaultProperties.put(GAME_CONFIGURATION, GAME_MAX_PLAYERS_PER_GAME + " = " + 4);
    defaultProperties.put(GAME_CONFIGURATION, GAME_DEFAULT_WIDTH + " = " + 50);
    defaultProperties.put(GAME_CONFIGURATION, GAME_DEFAULT_HEIGHT + " = " + 50);
    defaultProperties.put(METRICS_CONFIGURATION, METRICS_ENABLED + " = " + false);
    defaultProperties.put(METRICS_CONFIGURATION, CALCULATE_METRICS_INTERVAL + " = " + (1000 * 60));
    defaultProperties.put(METRICS_CONFIGURATION, METRICS_PUSH_UPDATE_RATE + " = " + (1000 * 60));
    defaultProperties.put(METRICS_CONFIGURATION, METRICS_INTERVAL_BETWEEN_SAVES + " = " + (1000 * 60));
    defaultProperties.put(MONGO_CONFIGURATION, MONGO_USERNAME + " = ");
    defaultProperties.put(MONGO_CONFIGURATION, MONGO_PASSWORD + " = ");
    defaultProperties.put(MONGO_CONFIGURATION, MONGO_DATABASE + " = ");
    defaultProperties.put(MONGO_CONFIGURATION, MONGO_HOST + " = ");
    defaultProperties.put(MONGO_CONFIGURATION, MONGO_PORT + " = ");
    defaultProperties.put(SERVER_CONFIGURATION, SERVER_PORT + " = " + 8000);
    defaultProperties.put(SERVER_CONFIGURATION, EXTERNAL_STATIC_FILES_PATH + " = ");
    defaultProperties.put(GAME_RUNNER_CONFIGURATION, GAMES_PER_THREAD + " = 20");
    defaultProperties.put(GAME_RUNNER_CONFIGURATION, GAME_ITERATION_INTERVAL + " = " + 250);
    ALL_DEFAULT_PROPERTIES = ImmutableListMultimap.copyOf(defaultProperties);
  }

  private final ConfigurationLoader configurationLoader;

  Configuration(ConfigurationLoader configurationLoader) {
    this.configurationLoader = Objects.requireNonNull(configurationLoader);
  }

  public float getInitialDensity() {
    return configurationLoader.getFloat(GAME_DEFAULT_INITIAL_DENSITY);
  }

  public long getGameDuration() {
    return configurationLoader.getLong(GAME_DURATION);
  }

  public int getMaxPlayers() {
    return configurationLoader.getInt(GAME_MAX_PLAYERS_PER_GAME);
  }

  public int getGridWidth() {
    return configurationLoader.getInt(GAME_DEFAULT_WIDTH);
  }

  public int getGridHeight() {
    return configurationLoader.getInt(GAME_DEFAULT_HEIGHT);
  }

  public int getTickRate() {
    return configurationLoader.getInt(GAME_ITERATION_INTERVAL);
  }

  public boolean isMetricsEnabled() {
    return configurationLoader.getBoolean(METRICS_ENABLED);
  }

  public long getCalculateMetricsInterval() {
    return configurationLoader.getLong(CALCULATE_METRICS_INTERVAL);
  }

  public long getMetricsPushInterval() {
    return configurationLoader.getLong(METRICS_PUSH_UPDATE_RATE);
  }

  public long metricsSaveInterval() {
    return configurationLoader.getLong(METRICS_INTERVAL_BETWEEN_SAVES);
  }

  public String getUsername() {
    return configurationLoader.getString(MONGO_USERNAME);
  }

  public char[] getPassword() {
    return configurationLoader.getString(MONGO_PASSWORD).toCharArray();
  }

  public String getDatabase() {
    return configurationLoader.getString(MONGO_DATABASE);
  }

  public String getHost() {
    return configurationLoader.getString(MONGO_HOST);
  }

  public int getDatabasePort() {
    return configurationLoader.getInt(MONGO_PORT);
  }

  public int getServerPort() {
    return configurationLoader.getInt(SERVER_PORT);
  }

  public String getExternalStaticFilesPath() {
    return configurationLoader.getString(EXTERNAL_STATIC_FILES_PATH);
  }

  public int getGamesPerThread() {
    return configurationLoader.getInt(GAMES_PER_THREAD);
  }

  static Multimap<String, String> getAllDefaultProperties() {
    return ALL_DEFAULT_PROPERTIES;
  }
}
