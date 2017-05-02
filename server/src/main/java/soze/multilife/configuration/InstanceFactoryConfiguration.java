package soze.multilife.configuration;

import soze.multilife.configuration.interfaces.InstanceRunnerConfiguration;

/**
 * Configuration for InstanceFactory.
 */
public class InstanceFactoryConfiguration implements InstanceRunnerConfiguration {

	private final Configuration configuration;

	protected InstanceFactoryConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public long getIterationInterval() {
		return configuration.getLong("gameIterationInterval");
	}
}
