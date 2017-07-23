package soze.multilife.configuration;

import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles configuration loading for the application.
 * All environmental variables are loaded, as well as all key-value pairs
 * from the specified file in CONFIG_PATH. If the app does not find
 * the config file, a message will be logged, but no exception will be thrown.
 * Environmental variables will overwrite properties in the file.
 */
class ConfigurationLoader {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoader.class);

	private static final Map<String, String> PROPERTIES = new HashMap<>();

	private static final String CONFIG_PATH = "app.cfg";

	private boolean loaded = false;

	void load() {
		loadPropertiesFromFile();
		//loading env variables last because they will overwrite
		loadEnvironmentalVariables();
		loaded = true;
	}

	int getInt(String propertyName) {
		checkLoaded();
		return Integer.parseInt(PROPERTIES.get(Objects.requireNonNull(propertyName)));
	}

	long getLong(String propertyName) {
		checkLoaded();
		return Long.parseLong(PROPERTIES.get(Objects.requireNonNull(propertyName)));
	}

	String getString(String propertyName) {
		checkLoaded();
		return PROPERTIES.get(Objects.requireNonNull(propertyName));
	}

	float getFloat(String propertyName) {
		checkLoaded();
		return Float.parseFloat(PROPERTIES.get(Objects.requireNonNull(propertyName)));
	}

	/**
	 * Throws a RuntimeException if configuration is not loaded.
	 */
	private void checkLoaded() {
		if (!loaded) {
			throw new RuntimeException("Load configuration first!");
		}
	}

	private void loadPropertiesFromFile() {
		prepareDefaultConfigFile();
		try {
			List<String> lines = Files.readAllLines(Paths.get(CONFIG_PATH));
			for (String line : lines) {
				parseConfigLine(line);
			}
		} catch (IOException e) {
			LOG.warn("Problem while reading config file.", e);
		}
	}

	/**
	 * Checks if config file exists, and if not creates it
	 * and populates it with all possible configuration options.
	 * For some of them, default values are supplied.
	 */
	private void prepareDefaultConfigFile() {
		Path configPath = Paths.get(CONFIG_PATH);
		boolean configExists = Files.exists(configPath);
		if(!configExists) {
			try {
				LOG.info("Configuration file does not exist, creating a new one.");
				Files.createFile(configPath);
				Files.write(configPath, getDefaultConfigurationLines());
			} catch (IOException e) {
				LOG.warn("Problem creating default config file.", e);
			}
			LOG.info("Successfully created default config file.");
		}
	}

	private List<String> getDefaultConfigurationLines() {
		List<String> configLines = new ArrayList<>();
		Multimap<String, String> defaultProperties = Configuration.getAllDefaultProperties();
		for(String group: defaultProperties.keySet()) {
			configLines.add("# " + group);
			Collection<String> properties = defaultProperties.get(group);
			configLines.addAll(properties);
		}
		return configLines;
	}

	private void parseConfigLine(String line) throws IOException {
		//ignore empty lines
		if(line.trim().length() == 0) {
			return;
		}
		if(line.charAt(0) == '#') {
			return;
		}
		String[] tokens = line.split("=");
		if (tokens.length != 2) {
			throw new IOException("Invalid property line, should be in key=value format. White space is ignored.");
		}
		PROPERTIES.put(tokens[0].trim(), tokens[1].trim());
	}

	private void loadEnvironmentalVariables() {
		PROPERTIES.putAll(System.getenv());
	}

}
