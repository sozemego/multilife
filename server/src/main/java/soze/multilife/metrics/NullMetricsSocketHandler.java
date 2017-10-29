package soze.multilife.metrics;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class NullMetricsSocketHandler implements MetricsWebSocketHandler {

  @Override
  public void onOpen(Session session) throws Exception {

  }

  @Override
  public void onClose(Session session, int statusCode, String reason) throws Exception {

  }

  @Override
  public void onMessage(Session session, String msg) throws Exception {

  }

  @Override
  public void run() {

  }
}
