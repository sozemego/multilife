package soze.multilife.messages.outgoing;

public class TimeRemainingMessage extends OutgoingMessage {

	public float remainingTime;

	public TimeRemainingMessage(float remainingTime) {
		setType(OutgoingType.TIME_REMAINING);
		this.remainingTime = remainingTime;
	}

	public float getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(float remainingTime) {
		this.remainingTime = remainingTime;
	}
}
