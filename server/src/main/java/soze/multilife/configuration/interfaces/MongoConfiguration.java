package soze.multilife.configuration.interfaces;

/**
 * Methods required by classes which initialize access to a mongoDB repository.
 */
public interface MongoConfiguration {

  public String getUsername();

  public char[] getPassword();

  public String getDatabase();

  public String getHost();

  public int getDatabasePort();

}
