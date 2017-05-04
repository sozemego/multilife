package soze.multilife.configuration;

import soze.multilife.configuration.interfaces.GameConfiguration;

/**
 * Configuration methods required by SimulationFactory.
 */
public class GameConfigurationImpl implements GameConfiguration {

	private final Configuration configuration;

	protected GameConfigurationImpl(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public long getGameDuration() {
		return configuration.getLong("gameDuration");
	}

	@Override
	public int getMaxPlayers() {
		return configuration.getInt("maxPlayersPerGame");
	}

	@Override
	public int getGridWidth() {
		return configuration.getInt("gameDefaultWidth");
	}

	@Override
	public int getGridHeight() {
		return configuration.getInt("gameDefaultHeight");
	}

	@Override
	public int getTickRate() {
		return configuration.getInt("gameIterationInterval");
	}
}
