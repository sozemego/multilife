package soze.multilife.server.connection.inward;

/**
 * Created by soze on 2/26/2017.
 */
public interface SerializedObjectConnection {

  /**
   * Returns id of this connection. Ids have to be unique.
   *
   * @return
   */
  public long getId();

  /**
   * Sends a message to the client connected to this connection.
   *
   * @param message
   */
  public void send(String message);

}
