package soze.multilife.configuration;

import soze.multilife.configuration.interfaces.MetricsConfiguration;

/**
 * Configuration for classes dealing with metrics.
 */
public class MetricsConfigurationImpl implements MetricsConfiguration {

	private final Configuration configuration;

	protected MetricsConfigurationImpl(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public long getCalculateMetricsInterval() {
		return configuration.getLong("calculateMetricsInterval");
	}

	@Override
	public long getMetricsPushInterval() {
		return configuration.getLong("metricsPushUpdateRate");
	}

	@Override
	public long metricsSaveInterval() {
		return configuration.getLong("metricsIntervalBetweenSaves");
	}
}
