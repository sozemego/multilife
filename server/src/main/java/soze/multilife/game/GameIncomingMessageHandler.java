package soze.multilife.game;


import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.messages.outgoing.PlayerData;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A game layer responsible for handling incoming messages.
 */
public class GameIncomingMessageHandler implements Game {

	private final Game game;

	/**
	 * Queue of messages received by this game.
	 */
	private final Queue<MessageQueueNode> queuedMessages = new ConcurrentLinkedQueue<>();

	public GameIncomingMessageHandler(Game game) {
		this.game = game;
	}

	public void run() {
		handleMessages();
		game.run();
	}

	public void acceptMessage(IncomingMessage message, long id) {
		queuedMessages.add(new MessageQueueNode(message, id));
	}

	private void handleMessages() {
		MessageQueueNode node;
		while ((node = queuedMessages.poll()) != null) {
			IncomingMessage message = node.getIncomingMessage();
			long id = node.getPlayerId();
			game.acceptMessage(message, id);
		}
	}

	private static class MessageQueueNode {

		private final IncomingMessage incomingMessage;
		private final long playerId;

		public MessageQueueNode(IncomingMessage incomingMessage, long playerId) {
			this.incomingMessage = incomingMessage;
			this.playerId = playerId;
		}

		public IncomingMessage getIncomingMessage() {
			return incomingMessage;
		}

		public long getPlayerId() {
			return playerId;
		}
	}

	//
	//
	// DELEGATED METHODS
	//
	//

	public int getId() {
		return game.getId();
	}

	public int getMaxPlayers() {
		return game.getMaxPlayers();
	}

	public int getWidth() {
		return game.getWidth();
	}

	public int getHeight() {
		return game.getHeight();
	}

	public Collection<Cell> getAllCells() {
		return game.getAllCells();
	}

	public boolean isOutOfTime() {
		return game.isOutOfTime();
	}

	public boolean isFull() {
		return game.isFull();
	}

	public void end() {
		game.end();
	}

	public long getTickRate() {
		return game.getTickRate();
	}

	public PlayerData getPlayerData() {
		return game.getPlayerData();
	}

	public Collection<Player> getPlayers() {
		return game.getPlayers();
	}

	public int getIterations() {
		return game.getIterations();
	}

	public long getRemainingTime() {
		return game.getRemainingTime();
	}

	public Collection<Cell> getClickedCells() {
		return game.getClickedCells();
	}

	public void addPlayer(Player player) {
		game.addPlayer(player);
	}

	public void removePlayer(long id) {
		game.removePlayer(id);
	}

	public void sendMessage(OutgoingMessage message) {
		game.sendMessage(message);
	}

	public boolean isScheduledForRemoval() {
		return game.isScheduledForRemoval();
	}

	//
	//
	// END DELEGATED METHODS
	//
	//

}
