package soze.multilife.server.connection;

/**
 * Class used for decorating connections.
 * @see LoggingConnection
 */
public class ConnectionDecorator implements Connection {

  private final Connection decorated;

  public ConnectionDecorator(Connection decorated) {
	this.decorated = decorated;
  }

  @Override
  public long getId() {
	return decorated.getId();
  }

  @Override
  public void send(String msg) {
	decorated.send(msg);
  }
}
