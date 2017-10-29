package soze.multilife.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.game.rule.RuleFactory;
import soze.multilife.messages.incoming.ClickMessage;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.incoming.IncomingType;
import soze.multilife.messages.outgoing.OutgoingMessage;

import java.awt.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;

/**
 * An object which handles the simulation ({@link Grid}) and players.
 * This object is responsible for advancing the simulation.
 */
public class BaseGame implements Game {

  private static final Logger LOG = LoggerFactory.getLogger(BaseGame.class);

  /**
   * Id of the AI player.
   */
  private static final int SIMULATION_PLAYER_ID = 0;

  /**
   * Id of this game.
   */
  private final int id;

  private boolean scheduledForRemoval;

  /**
   * Percent of alive cells spawned on simulation start.
   */
  private final float initialDensity;

  /**
   * Players already in-game.
   */
  private final Map<Integer, Player> players = new HashMap<>();

  private final int maxPlayers;

  /**
   * Maps playerId to color (#hex) of their alive cells.
   */
  private final Map<Integer, String> playerColors = new HashMap<>();

  /**
   * Colors which are available for players.
   */
  private final String[] availableColors = new String[]{"#ff0000", "#00ff00", "#0000ff", "#ffff00"};

  /**
   * Time for this instance to live in ms.
   */
  private final long duration;
  private long timePassed;
  private long t0 = -1;

  /**
   * Objects managing the cells.
   */
  private final Grid grid;

  /**
   * Cells clicked by players this iteration.
   */
  private final Set<Cell> clickedCells = new HashSet<>();

  /**
   * A set of playerIds who clicked cells this iteration.
   * For limiting purposes.
   */
  private final Set<Integer> clickedPlayers = new HashSet<>();

  /**
   * A count for how many times this simulation advanced.
   */
  private int iterations = 0;

  /**
   * Map of playerId-playerPoints.
   */
  private final Map<Integer, Integer> playerPoints = new HashMap<>();

  BaseGame(int id, float initialDensity, int width, int height, int maxPlayers, long duration) {
    this.id = id;
    this.initialDensity = initialDensity;
    this.grid = new Grid(width, height, RuleFactory.getRule("BASIC"));
    this.maxPlayers = maxPlayers;
    this.duration = duration;
    init();
  }

  /**
   * Spawns initial living cells.
   */
  private void init() {
    SecureRandom random = new SecureRandom();
    for (int i = 0; i < grid.getWidth(); i++) {
      for (int j = 0; j < grid.getHeight(); j++) {
        if (random.nextFloat() < initialDensity) {
          grid.changeState(i, j, true, SIMULATION_PLAYER_ID);
        }
      }
    }
    addCellStateChangeListeners();
    grid.updateGrid(); // runs the simulation once, so that the first player logging can receive some data
  }

  private void addCellStateChangeListeners() {
    grid.onCellDeath((strongestOwnerId) -> {
      if (strongestOwnerId == -1) {
        return;
      }
      Integer points = playerPoints.get(strongestOwnerId);
      playerPoints.put(strongestOwnerId, points == null ? 1 : ++points);
    });

    grid.onCellBirth((cellOwner) -> {
      Integer points = playerPoints.get(cellOwner);
      points = Math.max(points == null ? 0 : --points, 0);
      playerPoints.put(cellOwner, points);
    });
  }

  public int getId() {
    return id;
  }

  public boolean addPlayer(Player player) {
    if (getPlayers().size() == getMaxPlayers()) {
      return false;
    }
    //TODO should be getNextColor();
    players.put(player.getId(), player);
    playerColors.put(player.getId(), availableColors[players.size() % availableColors.length]);
    return true;
  }

  /**
   * Removes a player from the simulation.
   *
   * @param id id of the player to remove
   */
  public void removePlayer(int id) throws PlayerNotInGameException {
    checkContainsPlayer(id);
    grid.killAll(id);
    players.remove(id);
    playerPoints.remove(id);
  }

  /**
   * When a player sends an array of indices of cells they clicked,
   * this method checks if all of them are currently not alive.
   * If so, marks them as alive, sets their owner as the player who clicked
   * and sends information to all players about newly alive cells.
   * If a player already clicked cells during this iteration, this method returns.
   *
   * @param indices indices of cells to click
   * @param id      id of the player
   */
  private void click(int[] indices, int id) {
    if (clickedPlayers.contains(id)) { //only one click per iteration
      return;
    }
    LOG.trace("Player [{}] wants to click on [{}] cells.", id, indices.length);
    List<Cell> clickableCells = grid.findClickableCells(indices, id);
    if (clickableCells.size() == indices.length) {
      for (Cell cell : clickableCells) {
        if (clickedCells.contains(cell)) {
          return;
        }
      }
      clickedPlayers.add(id);
      clickedCells.addAll(clickableCells);
    }
  }

  /**
   * Updates the simulation. It sends data for newly logged in players,
   * advances the simulation by one iteration, sends data to players
   * and switches grids.
   */
  public void run() {
    updateTime();
    if (!players.isEmpty()) {
      grid.click(clickedCells);
      clickedCells.clear();
      clickedPlayers.clear();
      grid.updateGrid();
      iterations++;
    }
    if (isOutOfTime()) {
      setScheduledForRemoval(true);
    }
  }

  public Map<Integer, Integer> getPlayerPoints() {
    return new HashMap<>(playerPoints);
  }

  public String getPlayerColor(int playerId) {
    return playerColors.get(playerId);
  }

  public Map<Point, Cell> getAllCells() {
    return new HashMap<>(grid.getAllCells());
  }

  public void acceptMessage(IncomingMessage message, int playerId) throws PlayerNotInGameException {
    checkContainsPlayer(playerId);
    if (message.getType() == IncomingType.CLICK) {
      ClickMessage msg = (ClickMessage) message;
      click(msg.getIndices(), playerId);
    }
  }

  public long getRemainingTime() {
    return duration - timePassed;
  }

  public Collection<Cell> getClickedCells() {
    return new ArrayList<>(clickedCells);
  }

  public int getMaxPlayers() {
    return maxPlayers;
  }

  public boolean isOutOfTime() {
    return timePassed >= duration;
  }

  private void updateTime() {
    if (t0 == -1) {
      t0 = System.nanoTime();
    }
    timePassed += (System.nanoTime() - t0) / 1e6;
    t0 = System.nanoTime();
  }

  public boolean isFull() {
    return getMaxPlayers() == players.size();
  }

  public boolean isScheduledForRemoval() {
    return scheduledForRemoval;
  }

  private void setScheduledForRemoval(boolean scheduledForRemoval) {
    this.scheduledForRemoval = scheduledForRemoval;
  }

  public int getIterations() {
    return iterations;
  }

  public void sendMessage(OutgoingMessage message) {
    Objects.requireNonNull(message);
    for (Player p : getPlayers().values()) {
      p.send(message);
    }
  }

  /**
   * Ends the game, disconnects all players.
   */
  public void end() {
    synchronized (players) {
      List<Player> playersCopy = new ArrayList<>(players.values());
      for (Player player : playersCopy) {
        player.disconnect();
      }
    }
    setScheduledForRemoval(true);
  }

  public int getWidth() {
    return grid.getWidth();
  }

  public int getHeight() {
    return grid.getHeight();
  }

  public Map<Integer, Player> getPlayers() {
    return new HashMap<>(players);
  }

  private void checkContainsPlayer(int playerId) throws PlayerNotInGameException {
    Player player = players.get(playerId);
    if (player == null) {
      throw new PlayerNotInGameException(playerId);
    }
  }

  public String toString() {
    return "BaseGame{" +
      "id=" + id +
      '}';
  }
}
