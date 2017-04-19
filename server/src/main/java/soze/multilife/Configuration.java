package soze.multilife;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles configuration for the application.
 * All environmental variables are loaded, as well as all key-value pairs
 * from the specified file in CONFIG_PATH. If the app does not find
 * the config file, a message will be logged, but no exception will be thrown.
 * Environmental variables will overwrite properties in the file.
 */
public class Configuration {

  private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

  private static final Map<String, String> properties = new HashMap<>();

  private static final String CONFIG_PATH = "app.cfg";

  private boolean loaded = false;

  public int getInt(String propertyName) {
	checkLoaded();
	String value = properties.get(propertyName);
    return Integer.parseInt(value);
  }

  public long getLong(String propertyName) {
	checkLoaded();
	String value = properties.get(propertyName);
	return Long.parseLong(value);
  }

  public String getString(String propertyName) {
	checkLoaded();
	String value = properties.get(propertyName);
	return value;
  }

  private void checkLoaded() {
	if(!loaded) {
	  throw new RuntimeException("Load configuration first!");
	}
  }

  public void load() {
	loadPropertiesFromFile();
	//loading env variables last because they will overwrite
	loadEnvironmentalVariables();
    loaded = true;
  }

  private void loadPropertiesFromFile() {

	try {

	  List<String> lines = Files.readAllLines(Paths.get(CONFIG_PATH));
	  for (String line : lines) {
	    parseConfigLine(line);
	  }

	} catch (IOException e) {
	  LOG.warn("Problem while reading config file.", e);
	}

  }

  private void parseConfigLine(String line) throws IOException {

	String[] tokens = line.split("=");
	if (tokens.length != 2) {
	  throw new IOException("Invalid properties, not split with '='");
	}

	properties.put(tokens[0].trim(), tokens[1].trim());

  }

  private void loadEnvironmentalVariables() {

	Map<String, String> environmentalVariables = System.getenv();
	for(Map.Entry<String, String> entry: environmentalVariables.entrySet()) {
	  properties.put(entry.getKey(), entry.getValue());
	}

  }

}
