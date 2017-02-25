package soze.multilife;

import soze.multilife.server.Server;

import java.util.concurrent.ExecutionException;

/**
 * Created by soze on 2/20/2017.
 */
public class MultiLife {

  public static void main(String[] args) throws InterruptedException, ExecutionException {

    MultiLife ml = new MultiLife();
    ml.start();

  }

  private void start() throws InterruptedException, ExecutionException {
    Server server = new Server(8080);
    server.start();
  }

}
