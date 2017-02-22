package soze.multilife.server;

/**
 * A runnable for running instances.
 * It updates the instance and sleeps.
 */
public class InstanceRunner implements Runnable {

	private static final int TIME_INACTIVE_TO_DESTROY_IN_SECONDS = 60;
	private static final long TICK_TIME_MS = 250;

	private final Instance instance;
	/** Time this instance is inactive. Measured if this instance has no players. */
	private float timeInactive = 0;

	public InstanceRunner(Instance instance) {
		this.instance = instance;
	}

	@Override
	public void run() {
		System.out.println("InstanceRunner for instanceId " + instance.getId() + " was started.");
		long time = System.nanoTime();
		while(!instance.isScheduledForRemoval()) {

			instance.update();

			if(!instance.isActive()) {
				timeInactive += System.nanoTime() - time;
				if((timeInactive / 1e9) > TIME_INACTIVE_TO_DESTROY_IN_SECONDS) {
					instance.setScheduledForRemoval(true);
					System.out.println("After " + TIME_INACTIVE_TO_DESTROY_IN_SECONDS + " s of inactivity, instanceId " + instance.getId() + " was set to be removed");
				}
			} else {
				timeInactive = 0;
			}
			time = System.nanoTime();

			try {
				Thread.sleep(TICK_TIME_MS);
			} catch (InterruptedException e) {
				// dont stop this thread
			}

		}
	}

}
