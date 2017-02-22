package soze.multilife.server;

import soze.multilife.messages.incoming.ClickMessage;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.simulation.Player;
import soze.multilife.simulation.Simulation;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * An instance represents one room. Instances contain some players (up to a maximum amount).
 * Instances should be run by InstanceRunners.
 */
public class Instance {

  private final long id;
  private final Simulation simulation;
  private final Map<Long, Player> players = new HashMap<>();
  private final int maxPlayers;
  private boolean scheduledForRemoval;

  private final Queue<MessageQueueNode> queuedMessages = new ConcurrentLinkedQueue<>();

  public Instance(long id, Simulation simulation, int maxPlayers) {
    this.id = id;
    this.simulation = simulation;
    this.maxPlayers = maxPlayers;
  }

  public void update() {
    handleMessages();
    simulation.update();
  }

  public void addPlayer(Player player) {
    players.put(player.getId(), player);
    this.simulation.addFreshPlayer(player);
  }

  public void removePlayer(long id) {
    players.remove(id);
    simulation.removePlayer(id);
  }

  public void addMessage(IncomingMessage message, long id) {
    queuedMessages.add(new MessageQueueNode(message, id));
  }

  private void handleMessages() {
    MessageQueueNode node;
    while((node = queuedMessages.poll()) != null) {
      IncomingMessage message = node.getIncomingMessage();
      long id = node.getId();
      if(message.getType() == IncomingType.CLICK) {
        handleMessage((ClickMessage) message, id);
      }
    }
  }

  private void handleMessage(ClickMessage message, long id) {
    int index = message.getIndex();
    simulation.click(index, id);
  }

  public long getId() {
    return id;
  }

  public boolean isActive() {
    return !players.isEmpty();
  }

  public int getNumberOfPlayers() {
    return players.size();
  }

  public boolean isFull() {
    return getNumberOfPlayers() == maxPlayers;
  }

  public boolean isScheduledForRemoval() {
    return scheduledForRemoval;
  }

  public void setScheduledForRemoval(boolean scheduledForRemoval) {
    this.scheduledForRemoval = scheduledForRemoval;
  }

  public Simulation getSimulation() {
    return simulation;
  }

  private static class MessageQueueNode {

    private final IncomingMessage incomingMessage;
    private final long id;

    public MessageQueueNode(IncomingMessage incomingMessage, long id) {
      this.incomingMessage = incomingMessage;
      this.id = id;
    }

    public IncomingMessage getIncomingMessage() {
      return incomingMessage;
    }

    public long getId() {
      return id;
    }
  }

}
