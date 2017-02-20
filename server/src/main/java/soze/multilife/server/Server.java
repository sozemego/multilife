package soze.multilife.server;

import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by soze on 2/20/2017.
 */
public class Server {

	private final EventBus bus;

	public Server(int port, EventBus bus) throws IOException {
		this.server = new ServerSocket(port);
		this.bus = bus;
	}

	public void start() throws IOException {
		new Thread(new SocketListener(server, bus)).start();
	}

	public static class SocketListener implements Runnable {

		private final EventBus bus;

		SocketListener(ServerSocket server, EventBus bus) {
			this.server = server;
			this.bus = bus;
		}

		public void run() { //TODO https://github.com/webbit/webbit
			System.out.println("Now listening for connections!");
			while(true) {
				try {
					Socket socket = server.accept();
					bus.post(new ConnectionEvent(socket));
				} catch (IOException e) {
					e.printStackTrace(); // DONT KNOW WHAT TO DO YET!
				}
			}
		}
	}
}
