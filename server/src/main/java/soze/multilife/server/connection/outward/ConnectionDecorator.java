package soze.multilife.server.connection.outward;

import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.server.metrics.InstanceMetricsConnection;

/**
 * Class used for decorating connections.
 *
 * @see InstanceMetricsConnection
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
  public void send(OutgoingMessage msg) {
	decorated.send(msg);
  }

  protected Connection getConnection() {
	return decorated;
  }

}
