package soze.multilife.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A runnable for running instances.
 * It updates the instance and sleeps.
 */
public class InstanceRunner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(InstanceRunner.class);

	//TODO remove this entirely, games have a finite duration anyway
	private final long timeInactiveBeforeRemoval;
	private final long iterationInterval;

	private final Instance instance;
	/**
	 * Time this instance is inactive. Measured if this instance has no players.
	 */
	private float timeInactive = 0;

	public InstanceRunner(Instance instance, long iterationInterval, long timeInactiveBeforeRemoval) {
		this.instance = instance;
		this.iterationInterval = iterationInterval;
		this.timeInactiveBeforeRemoval = timeInactiveBeforeRemoval;
	}

	@Override
	public void run() {
		LOG.info("InstanceRunner for instanceId [{}] was started. ", instance.getId());
		long time = System.nanoTime();
		while (!instance.isScheduledForRemoval()) {

			instance.update();

			if (!instance.isActive()) {
				timeInactive += System.nanoTime() - time;
				if ((timeInactive / 1e9) > timeInactiveBeforeRemoval) {
					instance.setScheduledForRemoval(true);
					LOG.info("After [{} s] of inactivity instanceId [{}] was set to be removed. ", timeInactiveBeforeRemoval, instance.getId());
				}
			} else {
				timeInactive = 0;
			}
			time = System.nanoTime();

			if (instance.isOutOfTime()) {
				LOG.info("Game in instance [{}] has ended, closing.", instance.getId());
				break;
			}

			try {
				Thread.sleep(iterationInterval);
			} catch (InterruptedException e) {
				LOG.error("Instance runner was stopped. ", e);
			}

		}
	}

}
