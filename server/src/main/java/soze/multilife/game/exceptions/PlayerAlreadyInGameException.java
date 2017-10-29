package soze.multilife.game.exceptions;

/**
 * Thrown when trying to add a Player to a game and this Player is already in game.
 */
public class PlayerAlreadyInGameException extends Exception {

  private final long playerId;

  public PlayerAlreadyInGameException(long playerId) {
    this.playerId = playerId;
  }

  public long getPlayerId() {
    return playerId;
  }

}
