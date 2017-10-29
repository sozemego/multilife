package soze.multilife.game;

import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.server.connection.Connection;

import java.util.Objects;

public class Player {

  private final int id;
  private final Connection connection;
  private final String name;

  public Player(int id, Connection connection, String name) {
    this.id = id;
    this.connection = Objects.requireNonNull(connection);
    this.name = Objects.requireNonNull(name);
  }

  public int getId() {
    return id;
  }

  public void send(OutgoingMessage msg) {
    this.connection.send(msg);
  }

  public void send(byte[] bytes) {
    this.connection.send(bytes);
  }

  public void disconnect() {
    this.connection.disconnect();
  }

  public Connection getConnection() {
    return connection;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Player player = (Player) o;

    return id == player.id;
  }

  @Override
  public int hashCode() {
    return (id ^ (id >>> 32));
  }
}
