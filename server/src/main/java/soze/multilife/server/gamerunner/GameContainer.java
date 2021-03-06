package soze.multilife.server.gamerunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.game.Cell;
import soze.multilife.game.Game;
import soze.multilife.game.exceptions.PlayerNotInGameException;
import soze.multilife.messages.incoming.IncomingMessage;
import soze.multilife.messages.outgoing.CellList;
import soze.multilife.messages.outgoing.PlayerPoints;
import soze.multilife.messages.outgoing.TickData;
import soze.multilife.messages.outgoing.TimeRemainingMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameContainer implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(GameContainer.class);

  private final long id;

  private final Map<Integer, Game> games = new ConcurrentHashMap<>();

  private final long tickRate;

  private boolean isRunning = true;

  private final Queue<MessageQueueNode> queuedMessages = new ConcurrentLinkedQueue<>();

  public GameContainer(long id, long tickRate) {
    this.id = id;
    this.tickRate = tickRate;
  }

  public long getId() {
    return id;
  }

  public boolean addGame(Game game) {
    if (!isRunning()) {
      return false;
    }
    Objects.requireNonNull(game);
    games.put(game.getId(), game);
    return true;

  }

  public int getGamesCount() {
    return games.size();
  }

  public void stop() {
    this.isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public void acceptMessage(IncomingMessage message, int playerId, int gameId) {
    queuedMessages.add(new MessageQueueNode(message, playerId, gameId));
  }

  private void sendRemainingMessages(Game game) {
    TickData tickData = new TickData(game.getIterations());
    game.sendMessage(tickData);
    TimeRemainingMessage timeRemainingMessage = new TimeRemainingMessage(game.getRemainingTime());
    game.sendMessage(timeRemainingMessage);

    Map<Integer, Integer> playerPoints = game.getPlayerPoints();
    playerPoints.forEach((id, points) -> game.sendMessage(new PlayerPoints(id, points)));
  }

  public void run() {
    while (isRunning) {

      long startTime = 0;

      if (LOG.isTraceEnabled()) {
        startTime = System.nanoTime();
      }

      handleMessages();

      games.values()
        .stream()
        .filter(Game::isOutOfTime)
        .forEach(Game::end);

      games.values().removeIf(game -> game.isScheduledForRemoval() && game.getPlayers().isEmpty());
      games.values().forEach(this::sendClickedData);
      games.values().forEach(Game::run);
      games.values().forEach(this::sendRemainingMessages);

      if (LOG.isTraceEnabled()) {
        long totalTime = System.nanoTime() - startTime;
        LOG.trace("It took [{}] ms to run [{}] games", totalTime / 1e6, games.size());
      }

      try {
        Thread.sleep(this.tickRate);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if (games.isEmpty()) {
        stop();
      }
    }
  }

  private void handleMessages() {
    MessageQueueNode node;
    while ((node = queuedMessages.poll()) != null) {
      IncomingMessage message = node.getIncomingMessage();
      int id = node.getPlayerId();
      Game game = games.get(node.getGameId());
      if (game != null) {
        try {
          game.acceptMessage(message, id);
        } catch (PlayerNotInGameException e) {
          LOG.warn("Trying to pass a message to a player who is not in game. ");
        }
      }

    }
  }

  private void sendClickedData(Game game) {
    game.getPlayers().values().forEach(p -> {
      CellList cellList = constructCellList(game.getClickedCells());
      p.send(cellList);
    });
  }

  /**
   * Assembles {@link CellList} from a given list of cells.
   *
   * @param cells cells
   * @return CellList
   */
  private CellList constructCellList(Collection<Cell> cells) {
    List<CellList.CellData> cellData = new ArrayList<>();
    for (Cell cell : cells) {
      cellData.add(new CellList.CellData(cell));
    }
    return new CellList(cellData);
  }

}
