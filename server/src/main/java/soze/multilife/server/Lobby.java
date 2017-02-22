package soze.multilife.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import soze.multilife.messages.incoming.ClickMessage;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.incoming.LoginMessage;
import soze.multilife.messages.outgoing.OutgoingType;
import soze.multilife.messages.outgoing.PlayerIdentity;
import soze.multilife.simulation.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * A lobby. Connected, but not logged in users are stored here,
 * as well as all playing players. This object also starts new instances (simulations)
 * and assigns players to instances. It also passes messages along to appropriate rooms.
 */
public class Lobby implements Runnable {

  private final Map<Long, Connection> connections = new HashMap<>();
  private final Map<Long, Player> players = new HashMap<>();
  private final Map<Long, Instance> instances = new HashMap<>();
  private final Map<Long, Long> playerToInstance = new HashMap<>();
  private final InstanceFactory instanceFactory;

  public Lobby() {
    this.instanceFactory = new InstanceFactory(instances);
  }

  @Override
  public void run() {
    while(true) {

      /* Remove dead instances every ten minutes. */
      synchronized (instances) {
	boolean removed = instances.values().removeIf(Instance::isScheduledForRemoval);
	System.out.println("Lobby's sweepin', removed instances: " + removed);
      }

      try {
	Thread.sleep(1000 * 60 * 10);
      } catch (InterruptedException e) {
	e.printStackTrace();
      }
    }
  }

  /**
   * Called when a new player connects.
   * @param connection
   */
  void onConnect(Connection connection) {
    connections.put(connection.getId(), connection);
    connection.send(getPlayerIdentity(connection.getId()));
  }

  /**
   * Creates and stringifies a PersonIdentity object.
   * @param id
   * @return
   */
  private String getPlayerIdentity(long id) {
    PlayerIdentity playerIdentity = new PlayerIdentity(id);
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(playerIdentity);
    } catch (JsonProcessingException e) {
      return "";
    }
  }

  /**
   * Called when a player disconnects.
   * @param connection
   */
  void onDisconnect(Connection connection) {
    long id = connection.getId();
    connections.remove(id);
    players.remove(id);
    long instanceId = playerToInstance.remove(id);
    instances.get(instanceId).removePlayer(id);
  }

  /**
   * Handles incoming messages. Id is the connection id.
   * @param incMessage
   * @param id
   */
  void onMessage(IncomingMessage incMessage, long id) {
    if(incMessage.getType() == IncomingType.LOGIN) {
      handleLoginMessage((LoginMessage) incMessage, id);
      return;
    }
    long instanceId = playerToInstance.get(id);
    Instance instance = instances.get(instanceId);
    instance.addMessage(incMessage, id);
  }

  /**
   * Handles login message. Finds an instance with free space
   * or creates a new once.
   * @param message
   * @param id
   */
  private void handleLoginMessage(LoginMessage message, long id) {
    System.out.println("Player with name " + message.getName() + " is trying to login.");
    Player player = createPlayer(connections.get(id), message.getName(), message.getRule());
    players.put(id, player);
    Instance instance = instanceFactory.getInstance();
    instance.addPlayer(player);
    synchronized (instances) {
      instances.put(instance.getId(), instance);
    }
    playerToInstance.put(id, instance.getId());
  }

  private Player createPlayer(Connection connection, String name, String rule) {
    return new Player(connection.getId(), connection, name, rule);
  }

}
