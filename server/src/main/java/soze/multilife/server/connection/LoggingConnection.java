package soze.multilife.server.connection;

/**
 * A decorator for the connection, which allows logging
 * of sent (outbound) data.
 */
public class LoggingConnection extends ConnectionDecorator {

  public LoggingConnection(Connection decorated) {
	super(decorated);
  }

  @Override
  public long getId() {
	return super.getId();
  }

  @Override
  public void send(String msg) {
	super.send(msg);
  }
}
