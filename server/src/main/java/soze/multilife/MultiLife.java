package soze.multilife;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import soze.multilife.server.ConnectionEvent;
import soze.multilife.server.Server;

import java.io.IOException;

/**
 * Created by soze on 2/20/2017.
 */
public class MultiLife {

	public static void main(String[] args) throws IOException {

		MultiLife ml = new MultiLife();
		ml.start();

	}

	private final EventBus bus;

	private MultiLife() {
		this.bus = new EventBus("Main");
		this.bus.register(this);
	}

	private void start() throws IOException {
		Server server = new Server(8080, bus);
		server.start();
	}

	@Subscribe
	public void handleConnectionEvent(ConnectionEvent event) {


	}

}
