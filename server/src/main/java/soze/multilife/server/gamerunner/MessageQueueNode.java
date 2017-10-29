package soze.multilife.server.gamerunner;

import soze.multilife.messages.incoming.IncomingMessage;

class MessageQueueNode {

  private final IncomingMessage incomingMessage;
  private final int playerId;
  private final int gameId;

  MessageQueueNode(IncomingMessage incomingMessage, int playerId, int gameId) {
    this.incomingMessage = incomingMessage;
    this.playerId = playerId;
    this.gameId = gameId;
  }

  IncomingMessage getIncomingMessage() {
    return incomingMessage;
  }

  int getPlayerId() {
    return playerId;
  }

  public int getGameId() {
    return gameId;
  }
}
