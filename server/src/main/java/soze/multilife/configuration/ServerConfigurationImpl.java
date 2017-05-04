package soze.multilife.configuration;

import soze.multilife.configuration.interfaces.ServerConfiguration;

/**
 * Configuration for the server.
 */
public class ServerConfigurationImpl implements ServerConfiguration {

	private final Configuration configuration;

	public ServerConfigurationImpl(Configuration configuration) {
		this.configuration = configuration;
	}

	public int getPort() {
		return configuration.getInt("port");
	}

	public String getExternalStaticFilesPath() {
		return configuration.getString("externalStaticFilesPath");
	}
}
