package soze.multilife.server.connection;

import soze.multilife.server.metrics.MetricsConnection;

/**
 * Class used for decorating connections.
 * @see MetricsConnection
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

  protected Connection getConnection() {
    return decorated;
  }

}
