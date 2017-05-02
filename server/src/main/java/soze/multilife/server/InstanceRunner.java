package soze.multilife.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A runnable for running instances.
 * It updates the instance and sleeps.
 */
public class InstanceRunner implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(InstanceRunner.class);

	private final long iterationInterval;

	private final Instance instance;

	public InstanceRunner(Instance instance, long iterationInterval) {
		this.instance = instance;
		this.iterationInterval = iterationInterval;
	}

	@Override
	public void run() {
		LOG.info("InstanceRunner for instanceId [{}] was started. ", instance.getId());
		while (!instance.isScheduledForRemoval()) {

			instance.update();

			if (instance.isOutOfTime()) {
				LOG.info("Game in instance [{}] has ended, closing.", instance.getId());
				instance.setScheduledForRemoval(true);
			}

			try {
				Thread.sleep(iterationInterval);
			} catch (InterruptedException e) {
				LOG.error("Instance runner was stopped. ", e);
			}

		}
	}

}
