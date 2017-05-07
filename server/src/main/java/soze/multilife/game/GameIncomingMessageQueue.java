package soze.multilife.game;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.IncomingMessage;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A game layer responsible for handling incoming messages.
 */
public class GameIncomingMessageQueue extends GameDecorator {

	private static final Logger LOG = LoggerFactory.getLogger(GameIncomingMessageQueue.class);

	/**
	 * Queue of messages received by this game.
	 */
	private final Queue<MessageQueueNode> queuedMessages = new ConcurrentLinkedQueue<>();

	GameIncomingMessageQueue(Game game) {
		super(game);
	}

	public void run() {
		handleMessages();
		super.run();
	}

	public void acceptMessage(IncomingMessage message, long id) throws PlayerNotInGameException {
		checkPlayerInGame(id);
		queuedMessages.add(new MessageQueueNode(message, id));
	}

	private void checkPlayerInGame(long id) throws PlayerNotInGameException {
		boolean playerInGame = false;
		for (Player p : getPlayers()) {
			if(p.getId() == id) {
				playerInGame = true;
			}
		}
		if(!playerInGame) {
			throw new PlayerNotInGameException(id);
		}
	}

	private void handleMessages() {
		MessageQueueNode node;
		while ((node = queuedMessages.poll()) != null) {
			IncomingMessage message = node.getIncomingMessage();
			long id = node.getPlayerId();
			try {
				super.acceptMessage(message, id);
			} catch (PlayerNotInGameException e) {
				LOG.warn("Trying to pass a message to a player which is not in game. ");
			}
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
