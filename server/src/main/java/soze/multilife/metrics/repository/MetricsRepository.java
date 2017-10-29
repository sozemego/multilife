package soze.multilife.metrics.repository;

import java.time.Instant;
import java.util.Map;

/**
 * A interface for classes wanting to interact with metrics stored in a database.
 */
public interface MetricsRepository {

  void saveOutgoingKilobytesPerSecond(double kbs, long timestamp);

  void saveIncomingKilobytesPerSecond(double kbs, long timestamp);

  void saveMaxPlayers(int players, long timestamp);

  Map<Long, Double> getAverageKbsOutgoingSince(Instant instant);

  Map<Long, Double> getAverageKbsIncomingSince(Instant timeSince);
}
