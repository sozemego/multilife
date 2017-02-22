package soze.multilife;

import soze.multilife.server.Server;
import soze.multilife.server.GameSocketHandler;
import soze.multilife.server.Lobby;

import java.util.concurrent.ExecutionException;

/**
 * Created by soze on 2/20/2017.
 */
public class MultiLife {

  public static void main(String[] args) throws InterruptedException, ExecutionException {

    MultiLife ml = new MultiLife();
    ml.start();

  }

  private final Lobby lobby;

  private MultiLife() {
    this.lobby = new Lobby();
    new Thread(lobby).start();
  }

  private void start() throws InterruptedException, ExecutionException {
    Server server = new Server(8080, new GameSocketHandler(this.lobby));
    server.start();
  }

}
