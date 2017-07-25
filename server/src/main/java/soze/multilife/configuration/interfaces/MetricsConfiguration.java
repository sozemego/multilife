package soze.multilife.configuration.interfaces;

/**
 * Configuration for classes which report, analize or otherwise calculate various metrics.
 */
public interface MetricsConfiguration {

	public boolean isMetricsEnabled();
	public long getCalculateMetricsInterval();
	public long getMetricsPushInterval();
	public long metricsSaveInterval();

}
