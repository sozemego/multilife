package soze.multilife.metrics.events;

import soze.multilife.metrics.service.MetricsServiceImpl;

public class PlayerDisconnectedEvent implements MetricEvent {

  private final int playerId;

  public PlayerDisconnectedEvent(int playerId) {
    this.playerId = playerId;
  }

  public int getPlayerId() {
    return playerId;
  }

  public void accept(MetricsServiceImpl.MetricEventVisitor visitor) {
    visitor.visit(this);
  }
}
