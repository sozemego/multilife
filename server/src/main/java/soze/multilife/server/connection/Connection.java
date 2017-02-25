package soze.multilife.server.connection;

/**
 * A bridge for all types of connections.
 */
public interface Connection {

  /**
   * Returns id of this connection. Ids have to be unique.
   * @return
   */
  public long getId();

  /**
   * Sends a message to the client connected to this connection.
   * @param msg
   */
  public void send(String msg);

}
