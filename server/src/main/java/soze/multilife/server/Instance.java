package soze.multilife.server;

import soze.multilife.messages.incoming.ClickMessage;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.outgoing.TimeRemainingMessage;
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

  /**
   * Time for this instance to live in ms.
   */
  private final long timeToLive;
  private long timePassed;
  private long t0 = -1;


  private final Queue<MessageQueueNode> queuedMessages = new ConcurrentLinkedQueue<>();

  public Instance(long id, Simulation simulation, int maxPlayers, long timeToLive) {
	this.id = id;
	this.simulation = simulation;
	this.maxPlayers = maxPlayers;
	this.timeToLive = timeToLive;
  }

  public void update() {
	updateTime();
	handleMessages();
	simulation.update();
	sendRemainingTime();
  }

  public void addPlayer(Player player) {
	players.put(player.getId(), player);
	this.simulation.addPlayer(player);
  }

  public void removePlayer(long id) {
	players.remove(id);
	simulation.removePlayer(id);
  }

  public void addMessage(IncomingMessage message, long id) {
	queuedMessages.add(new MessageQueueNode(message, id));
  }

  public boolean isOutOfTime() {
    return timePassed > timeToLive;
  }

  private void updateTime() {
	if(t0 == -1) {
	  t0 = System.nanoTime();
	}
	timePassed += (System.nanoTime() - t0) / 1e6;
	t0 = System.nanoTime();
  }

  private void handleMessages() {
	MessageQueueNode node;
	while ((node = queuedMessages.poll()) != null) {
	  IncomingMessage message = node.getIncomingMessage();
	  long id = node.getId();
	  if (message.getType() == IncomingType.CLICK) {
		handleMessage((ClickMessage) message, id);
	  }
	}
  }

  private void handleMessage(ClickMessage message, long id) {
	int[] indices = message.getIndices();
	simulation.click(indices, id);
  }

  private void sendRemainingTime() {
	TimeRemainingMessage timeRemainingMessage = new TimeRemainingMessage(timeToLive - timePassed);
    synchronized (players) {
      for(Player player: players.values()) {
        player.send(timeRemainingMessage);
	  }
	}
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
