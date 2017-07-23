package soze.multilife.configuration;

import soze.multilife.configuration.interfaces.GameConfiguration;
import soze.multilife.configuration.interfaces.MetricsConfiguration;
import soze.multilife.configuration.interfaces.MongoConfiguration;
import soze.multilife.configuration.interfaces.ServerConfiguration;

import java.util.Objects;

public class Configuration
		implements GameConfiguration, MetricsConfiguration, MongoConfiguration, ServerConfiguration {

	private final ConfigurationLoader configurationLoader;

	Configuration(ConfigurationLoader configurationLoader) {
		this.configurationLoader = Objects.requireNonNull(configurationLoader);
	}

	public float getInitialDensity() {
		return configurationLoader.getFloat("gameDefaultInitialDensity");
	}

	public long getGameDuration() {
		return configurationLoader.getLong("gameDuration");
	}

	public int getMaxPlayers() {
		return configurationLoader.getInt("maxPlayersPerGame");
	}

	public int getGridWidth() {
		return configurationLoader.getInt("gameDefaultWidth");
	}

	public int getGridHeight() {
		return configurationLoader.getInt("gameDefaultHeight");
	}

	public int getTickRate() {
		return configurationLoader.getInt("gameIterationInterval");
	}

	public long getCalculateMetricsInterval() {
		return configurationLoader.getLong("calculateMetricsInterval");
	}

	public long getMetricsPushInterval() {
		return configurationLoader.getLong("metricsPushUpdateRate");
	}

	public long metricsSaveInterval() {
		return configurationLoader.getLong("metricsIntervalBetweenSaves");
	}

	public String getUsername() {
		return configurationLoader.getString("mongoUsername");
	}

	public char[] getPassword() {
		return configurationLoader.getString("mongoPassword").toCharArray();
	}

	public String getDatabase() {
		return configurationLoader.getString("mongoDatabase");
	}

	public String getHost() {
		return configurationLoader.getString("mongoHost");
	}

	public int getDatabasePort() {
		return configurationLoader.getInt("mongoPort");
	}

	public int getServerPort() {
		return configurationLoader.getInt("port");
	}

	public String getExternalStaticFilesPath() {
		return configurationLoader.getString("externalStaticFilesPath");
	}
}
