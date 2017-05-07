package soze.multilife.game;

import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.messages.outgoing.PlayerData;

import java.util.Collection;

public interface Game extends Runnable {

	/**
	 * Returns id of this game. Ids should be unique.
	 * @return
	 */
	int getId();

	/**
	 * Processes an incoming message. Those messages are sent from the client.
	 * PlayerId denotes id of the player who sent the message.
	 * @param message
	 * @param playerId
	 */
	void acceptMessage(IncomingMessage message, long playerId);

	/**
	 * Adds a player to the game.
	 * @param player
	 * @return true if player was added, false if game was full
	 */
	boolean addPlayer(Player player);

	/**
	 * Removes a player with given id from the game.
	 * @param id
	 */
	void removePlayer(long id);

	/**
	 * Ends the game. Disconnects all players and schedules this game for removal.
	 */
	void end();

	/**
	 * Returns time in milliseonds between game updates.
	 * @return
	 */
	long getTickRate();

	int getMaxPlayers();

	/**
	 * Creates {@link PlayerData} object.
	 * This object contains information about player points, names, colors and rules.
	 *
	 * @return PlayerData
	 */
	PlayerData getPlayerData();

	/**
	 * Returns a collection of players connected to the game.
	 * @return
	 */
	Collection<Player> getPlayers();

	int getWidth();
	int getHeight();

	boolean isFull();

	/**
	 * Returns number of iterations this game has gone through so far.
	 * @return
	 */
	int getIterations();

	/**
	 * Returns remaining time in milliseconds.
	 * @return
	 */
	long getRemainingTime();
	boolean isOutOfTime();

	Collection<Cell> getClickedCells();
	Collection<Cell> getAllCells();

	/**
	 * Sends the given message to all players connected to the game.
	 * @param message
	 */
	void sendMessage(OutgoingMessage message);

	boolean isScheduledForRemoval();

}
