package soze.multilife.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs uncaught exceptions thrown by threads.
 */
public class UncaughtExceptionLogger implements Thread.UncaughtExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(UncaughtExceptionLogger.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		LOG.error("Thread [{}], threw an exception [{}]. [{}]", t, e, e.getStackTrace());
	}
}
