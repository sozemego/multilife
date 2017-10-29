package soze.multilife.messages.outgoing;

import java.util.Objects;

public class PlayerAdded extends OutgoingMessage {

  private final int playerId;
  private final int playerColor;
  private final String playerName;

  public PlayerAdded(int playerId, String playerColor, String playerName) {
    setType(OutgoingType.PLAYER_ADDED);
    this.playerId = playerId;
    this.playerColor = this.convertStringColorToInt(playerColor);
    this.playerName = Objects.requireNonNull(playerName);
  }

  private int convertStringColorToInt(String playerColor) {
    Objects.requireNonNull(playerColor);
    return Integer.parseInt(stripHash(playerColor), 16);
  }

  private String stripHash(String playerColor) {
    if (playerColor.charAt(0) == '#') {
      return playerColor.substring(1);
    }
    return playerColor;
  }

  public void accept(OutgoingMessageVisitor visitor) {
    visitor.visit(this);
  }

  public int getPlayerId() {
    return playerId;
  }

  public int getPlayerColor() {
    return playerColor;
  }

  public String getPlayerName() {
    return playerName;
  }

  @Override
  public String toString() {
    return "PlayerAdded{" +
      "playerId=" + playerId +
      ", playerColor=" + playerColor +
      ", playerName='" + playerName + '\'' +
      '}';
  }
}
