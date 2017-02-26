package soze.multilife.server.connection.inward;

/**
 * Created by soze on 2/26/2017.
 */
public class SerializedObjectConnectionDecorator implements SerializedObjectConnection {

  private final SerializedObjectConnection serializedObjectConnection;

  public SerializedObjectConnectionDecorator(SerializedObjectConnection serializedObjectConnection) {
	this.serializedObjectConnection = serializedObjectConnection;
  }

  @Override
  public long getId() {
	return serializedObjectConnection.getId();
  }

  @Override
  public void send(String message) {
	serializedObjectConnection.send(message);
  }

  protected SerializedObjectConnection getConnection() {
    return serializedObjectConnection;
  }
}
