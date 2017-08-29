package soze.multilife.metrics.events;

import soze.multilife.metrics.MetricsService;

/**
 * Supertype for all metric events.
 */
public interface MetricEvent {

	public void accept(MetricsService.MetricEventVisitor visitor);

}
