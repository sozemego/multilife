package soze.multilife.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A runnable for running instances.
 * It updates the instance and sleeps.
 */
public class InstanceRunner implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(InstanceRunner.class);

  private static final int TIME_INACTIVE_TO_DESTROY_IN_SECONDS = 15;
  private static final long TICK_TIME_MS = 250;

  private final Instance instance;
  /**
   * Time this instance is inactive. Measured if this instance has no players.
   */
  private float timeInactive = 0;

  public InstanceRunner(Instance instance) {
	this.instance = instance;
  }

  @Override
  public void run() {
	LOG.info("InstanceRunner for instanceId [{}] was started. ", instance.getId());
	long time = System.nanoTime();
	while (!instance.isScheduledForRemoval()) {

	  instance.update();

	  if (!instance.isActive()) {
		timeInactive += System.nanoTime() - time;
		if ((timeInactive / 1e9) > TIME_INACTIVE_TO_DESTROY_IN_SECONDS) {
		  instance.setScheduledForRemoval(true);
		  LOG.info("After [{} s] of inactivity instanceId [{}] was set to be removed. ", TIME_INACTIVE_TO_DESTROY_IN_SECONDS, instance.getId());
		}
	  } else {
		timeInactive = 0;
	  }
	  time = System.nanoTime();

	  if(instance.isOutOfTime()) {
	    LOG.info("Game in instance [{}] has ended, closing.", instance.getId());
	    break;
	  }

	  try {
		Thread.sleep(TICK_TIME_MS);
	  } catch (InterruptedException e) {
		LOG.error("Instance runner was stopped. ", e);
	  }

	}
  }

}
