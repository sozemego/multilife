package soze.multilife.configuration;

/**
 * Reads configuration from file and returns classes which expose a narrow, specialized
 * set of configuration.
 */
public class ConfigurationFactory {

	private final Configuration configuration;

	public ConfigurationFactory() {
		this.configuration = new Configuration();
		this.configuration.load();
	}

	public GameConfigurationImpl getGameConfiguration() {
		return new GameConfigurationImpl(configuration);
	}

	public MongoConfigurationImpl getMongoConfiguration() {
		return new MongoConfigurationImpl(configuration);
	}

	public MetricsConfigurationImpl getMetricsConfiguration() {
		return new MetricsConfigurationImpl(configuration);
	}

	public ServerConfigurationImpl getServerConfiguration() {
		return new ServerConfigurationImpl(configuration);
	}

}
