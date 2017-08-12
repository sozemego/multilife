package soze.multilife.configuration.interfaces;

/**
 * Configuration for a single game. Variables like game length, max players etc.
 */
public interface GameConfiguration {

	public float getInitialDensity();
	public long getGameDuration();
	public int getMaxPlayers();
	public int getGridWidth();
	public int getGridHeight();
}
