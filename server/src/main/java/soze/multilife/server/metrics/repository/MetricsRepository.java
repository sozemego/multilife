package soze.multilife.server.metrics.repository;

/**
 * A interface for classes wanting to interact with metrics stored in a database.
 */
public interface MetricsRepository {

	void saveKilobytesPerSecond(double kbs, long timestamp);
	void saveMaxPlayers(int players, long timestamp);

}
