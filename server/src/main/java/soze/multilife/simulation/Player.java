package soze.multilife.simulation;

import soze.multilife.server.Connection;

/**
 * Created by soze on 2/21/2017.
 */
public class Player {

  private final long id;
  private final Connection connection;
  private final String name;
  private final String rule;

  public Player(long id, Connection connection, String name, String rule) {
    this.id = id;
    this.connection = connection;
    this.name = name;
    this.rule = rule;
  }

  public long getId() {
    return id;
  }

  public void send(String msg) {
    this.connection.send(msg);
  }

  public Connection getConnection() {
    return connection;
  }

  public String getName() {
    return name;
  }

  public String getRule() {
    return rule;
  }
}