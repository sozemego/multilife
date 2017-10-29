package soze.multilife.game.exceptions;

/**
 * Thrown when a method related to the player is called, but the player is not in game.
 */
public class PlayerNotInGameException extends Exception {

  private final long playerId;

  public PlayerNotInGameException(long playerId) {
    this.playerId = playerId;
  }

  public long getPlayerId() {
    return playerId;
  }

}
