package soze.multilife.configuration;

import soze.multilife.configuration.interfaces.*;

/**
 * Reads configuration from file and returns classes which expose a narrow, specialized
 * set of configuration.
 */
public class ConfigurationFactory {

  private final ConfigurationLoader configurationLoader;
  private final Configuration configuration;

  public ConfigurationFactory() {
    this.configurationLoader = new ConfigurationLoader();
    this.configurationLoader.load();
    this.configuration = new Configuration(this.configurationLoader);
  }

  public GameConfiguration getGameConfiguration() {
    return this.configuration;
  }

  public MongoConfiguration getMongoConfiguration() {
    return this.configuration;
  }

  public MetricsConfiguration getMetricsConfiguration() {
    return this.configuration;
  }

  public ServerConfiguration getServerConfiguration() {
    return this.configuration;
  }

  public GameRunnerConfiguration getGameRunnerConfiguration() {
    return this.configuration;
  }

}
