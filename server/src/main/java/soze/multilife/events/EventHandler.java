package soze.multilife.events;

/**
 * An interface for event handlers. The interface follows the publish-subscribe pattern.
 * Provides methods for (un)registering observers
 * and posting events. Closely follows the EventBus (google guava)
 * interface. This is not supposed to be an interface implemented by publishers
 * or subscribers, but by an intermediate object - one which relays
 * events from publishers to subscribers.
 */
public interface EventHandler {

	/**
	 * Registers an observer. Same observer cannot be registered twice.
	 *
	 * @param observer
	 */
	public void register(Object observer);

	/**
	 * Unregisters an observer.
	 *
	 * @param observer
	 */
	public void unregister(Object observer);

	/**
	 * Posts an event. This event will be passed down to every
	 * observer subscribing to its type.
	 *
	 * @param event
	 */
	public void post(Object event);

}
