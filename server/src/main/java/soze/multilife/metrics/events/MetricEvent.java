package soze.multilife.metrics.events;

import soze.multilife.metrics.service.MetricsServiceImpl;

/**
 * Supertype for all metric events.
 */
public interface MetricEvent {

  public void accept(MetricsServiceImpl.MetricEventVisitor visitor);

}
