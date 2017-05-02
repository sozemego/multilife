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

	public SimulationFactoryConfiguration getSimulationFactoryConfiguration() {
		return new SimulationFactoryConfiguration(configuration);
	}

	public InstanceFactoryConfiguration getInstanceFactoryConfiguration() {
		return new InstanceFactoryConfiguration(configuration);
	}

	public MongoConfigurationImpl getMongoConfiguration() {
		return new MongoConfigurationImpl(configuration);
	}

	public MetricsConfigurationImpl getMetricsConfiguration() {
		return new MetricsConfigurationImpl(configuration);
	}

}
