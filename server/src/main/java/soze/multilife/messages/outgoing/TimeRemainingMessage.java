package soze.multilife.messages.outgoing;

public class TimeRemainingMessage extends OutgoingMessage {

  public long remainingTime;

  public TimeRemainingMessage(long remainingTime) {
    setType(OutgoingType.TIME_REMAINING);
    this.remainingTime = remainingTime;
  }

  public long getRemainingTime() {
	return remainingTime;
  }

  public void setRemainingTime(long remainingTime) {
	this.remainingTime = remainingTime;
  }
}
