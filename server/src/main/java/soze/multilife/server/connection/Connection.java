package soze.multilife.server.connection;

import soze.multilife.messages.outgoing.OutgoingMessage;

/**
 * A bridge for all types of connections.
 */
public interface Connection {

  /**
   * Returns id of this connection. Ids have to be unique.
   */
  public int getId();

  /**
   * Sends a message to the client connected to this connection.
   * Implementing classes should serialize the data in an appropriate way
   * (to be compatible with front-end).
   */
  public void send(OutgoingMessage message);

  public void send(byte[] bytes);

  /**
   * Terminates this connection.
   */
  public void disconnect();

}
