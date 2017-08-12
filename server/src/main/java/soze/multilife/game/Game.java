package soze.multilife.game;

import soze.multilife.game.exceptions.PlayerAlreadyInGameException;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.messages.outgoing.PlayerData;

import java.awt.*;
import java.util.Collection;
import java.util.Map;

public interface Game {

	/**
	 * Returns id of this game. Ids should be unique.
	 */
	int getId();

	/**
	 * Processes an incoming message. Those messages are sent from the client.
	 * PlayerId denotes id of the player who sent the message.
	 * @throws PlayerNotInGameException if player with given id is not in game
	 */
	void acceptMessage(IncomingMessage message, long playerId) throws PlayerNotInGameException;

	/**
	 * Adds a player to the game.
	 * @return true if player was added, false if game was full
	 * @throws PlayerAlreadyInGameException if this player is already in game
	 */
	boolean addPlayer(Player player) throws PlayerAlreadyInGameException;

	/**
	 * Removes a player with given id from the game.
	 * @throws PlayerNotInGameException if a player with given id is not in game
	 */
	void removePlayer(long playerId) throws PlayerNotInGameException;

	/**
	 * Ends the game. Disconnects all players and schedules this game for removal.
	 */
	void end();

	int getMaxPlayers();

	/**
	 * Creates {@link PlayerData} object.
	 * This object contains information about player points, names, colors and rules.
	 *
	 * @return PlayerData
	 */
	PlayerData getPlayerData();

	/**
	 * Returns a map of playerId-player pairs connected to the game.
	 */
	Map<Long, Player> getPlayers();

	int getWidth();
	int getHeight();

	boolean isFull();

	/**
	 * Returns number of iterations this game has gone through so far.
	 */
	int getIterations();

	/**
	 * Returns remaining time in milliseconds.
	 */
	long getRemainingTime();
	boolean isOutOfTime();

	Collection<Cell> getClickedCells();
	Map<Point, Cell> getAllCells();

	/**
	 * Sends the given message to all players connected to the game.
	 */
	void sendMessage(OutgoingMessage message);

	boolean isScheduledForRemoval();

	public void run();

}
