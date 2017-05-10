package soze.multilife.configuration;

import soze.multilife.configuration.interfaces.MongoConfiguration;

/**
 * Configuration for MongoDB.
 */
public class MongoConfigurationImpl implements MongoConfiguration {

	private final Configuration configuration;

	protected MongoConfigurationImpl(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getUsername() {
		return configuration.getString("mongoUsername");
	}

	@Override
	public char[] getPassword() {
		return configuration.getString("mongoPassword").toCharArray();
	}

	@Override
	public String getDatabase() {
		return configuration.getString("mongoDatabase");
	}

	@Override
	public String getHost() {
		return configuration.getString("mongoHost");
	}

	@Override
	public int getPort() {
		return configuration.getInt("mongoPort");
	}
}
