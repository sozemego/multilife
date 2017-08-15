package soze.multilife.game;

import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.server.connection.Connection;

import java.util.Objects;

public class Player {

	private final long id;
	private final Connection connection;
	private final String name;
	private final String rule;

	public Player(long id, Connection connection, String name, String rule) {
		this.id = id;
		this.connection = Objects.requireNonNull(connection);
		this.name = Objects.requireNonNull(name);
		this.rule = Objects.requireNonNull(rule);
	}

	public long getId() {
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

	public String getRule() {
		return rule;
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
		return (int) (id ^ (id >>> 32));
	}
}
