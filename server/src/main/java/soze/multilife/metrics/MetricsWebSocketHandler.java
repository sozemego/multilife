package soze.multilife.metrics;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public interface MetricsWebSocketHandler extends Runnable {

	@OnWebSocketConnect
	void onOpen(Session session) throws Exception;

	@OnWebSocketClose
	void onClose(Session session, int statusCode, String reason) throws Exception;

	@OnWebSocketMessage
	void onMessage(Session session, String msg) throws Exception;

}
