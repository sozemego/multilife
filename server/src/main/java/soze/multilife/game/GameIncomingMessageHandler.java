package soze.multilife.game;


import soze.multilife.messages.incoming.IncomingMessage;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A game layer responsible for handling incoming messages.
 */
public class GameIncomingMessageHandler extends GameDecorator {

	/**
	 * Queue of messages received by this game.
	 */
	private final Queue<MessageQueueNode> queuedMessages = new ConcurrentLinkedQueue<>();

	GameIncomingMessageHandler(Game game) {
		super(game);
	}

	public void run() {
		handleMessages();
		super.run();
	}

	public void acceptMessage(IncomingMessage message, long id) {
		queuedMessages.add(new MessageQueueNode(message, id));
	}

	private void handleMessages() {
		MessageQueueNode node;
		while ((node = queuedMessages.poll()) != null) {
			IncomingMessage message = node.getIncomingMessage();
			long id = node.getPlayerId();
			super.acceptMessage(message, id);
		}
	}

	private static class MessageQueueNode {

		private final IncomingMessage incomingMessage;
		private final long playerId;

		MessageQueueNode(IncomingMessage incomingMessage, long playerId) {
			this.incomingMessage = incomingMessage;
			this.playerId = playerId;
		}

		IncomingMessage getIncomingMessage() {
			return incomingMessage;
		}

		long getPlayerId() {
			return playerId;
		}
	}

}
