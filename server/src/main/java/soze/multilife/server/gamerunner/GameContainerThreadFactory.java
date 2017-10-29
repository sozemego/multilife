package soze.multilife.server.gamerunner;

import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

final class GameContainerThreadFactory implements ThreadFactory {

  private final String namePrefix;
  private final AtomicLong idGenerator = new AtomicLong(1L);

  GameContainerThreadFactory(String namePrefix) {
    this.namePrefix = Objects.requireNonNull(namePrefix);
  }

  @Override
  public Thread newThread(Runnable r) {
    return new Thread(r, namePrefix + "-" + idGenerator.getAndIncrement());
  }
}
