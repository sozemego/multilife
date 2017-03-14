package soze.multilife.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.events.EventHandler;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.messages.outgoing.PlayerIdentity;
import soze.multilife.messages.outgoing.PongMessage;
import soze.multilife.server.connection.Connection;
import soze.multilife.server.metrics.events.PlayerDisconnectedEvent;
import soze.multilife.server.metrics.events.PlayerLoggedEvent;
import soze.multilife.simulation.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A lobby. Connected, but not logged in users are stored here,
 * as well as all playing players. This object also starts new instances (simulations)
 * and assigns players to instances. It also passes messages along to appropriate rooms.
 */
public class Lobby implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(Lobby.class);

  private final Map<Long, Connection> connections = new HashMap<>();
  private final Map<Long, Player> players = new HashMap<>();
  private final Map<Long, Instance> instances = new HashMap<>();
  private final Map<Long, Long> playerToInstance = new HashMap<>();
  private final InstanceFactory instanceFactory;

  private final EventHandler eventHandler;

  public Lobby(EventHandler eventHandler) {
	this.eventHandler = eventHandler;
	this.instanceFactory = new InstanceFactory(instances);
  }

  @Override
  public void run() {
	while (true) {

	  synchronized (instances) {
		List<Long> instancesScheduledForRemoval =
		  instances
			.values()
			.stream()
			.filter(Instance::isScheduledForRemoval)
			.map(Instance::getId)
			.collect(Collectors.toList());
		instancesScheduledForRemoval.forEach(instances::remove);
		LOG.trace("Lobby's sweepin', removed [{}] instances.", instancesScheduledForRemoval.size());
	  }

	  try {
		Thread.sleep(1000 * 15);
	  } catch (InterruptedException e) {
		e.printStackTrace();
	  }
	}
  }

  /**
   * Called when a new player connects.
   *
   * @param connection
   */
  void onConnect(Connection connection) {
	connections.put(connection.getId(), connection);
	connection.send(getPlayerIdentity(connection.getId()));
  }

  /**
   * Creates a PersonIdentity object.
   *
   * @param id unique id of the player
   * @return
   */
  private PlayerIdentity getPlayerIdentity(long id) {
	return new PlayerIdentity(id);
  }

  /**
   * Called when a player disconnects.
   *
   * @param connection
   */
  void onDisconnect(Connection connection) {
	long id = connection.getId();
	connections.remove(id);
	players.remove(id);
	long instanceId = playerToInstance.remove(id);
	instances.get(instanceId).removePlayer(id);
	eventHandler.post(new PlayerDisconnectedEvent(id));
  }

  /**
   * Handles incoming messages. Id is the connection id.
   *
   * @param incMessage
   * @param id
   */
  void onMessage(IncomingMessage incMessage, long id) {
	if (incMessage.getType() == IncomingType.LOGIN) {
	  handleLoginMessage((LoginMessage) incMessage, id);
	  return;
	}
	if (incMessage.getType() == IncomingType.PING) {
	  connections.get(id).send(new PongMessage());
	  return;
	}
	long instanceId = playerToInstance.get(id);
	Instance instance = instances.get(instanceId);
	instance.addMessage(incMessage, id);
  }

  /**
   * Handles login message. Finds an instance with free space
   * or creates a new once.
   *
   * @param message
   * @param id
   */
  private void handleLoginMessage(LoginMessage message, long id) {
	LOG.info("Player with name [{}] is trying to login. ", message.getName());
	Player player = createPlayer(connections.get(id), message.getName(), message.getRule());
	players.put(id, player);
	Instance instance = instanceFactory.getInstance();
	instance.addPlayer(player);
	synchronized (instances) {
	  instances.put(instance.getId(), instance);
	}
	playerToInstance.put(id, instance.getId());
	eventHandler.post(new PlayerLoggedEvent(id, instance.getId()));
  }

  private Player createPlayer(Connection connection, String name, String rule) {
	return new Player(connection.getId(), connection, name, rule);
  }

}
