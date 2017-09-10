package soze.multilife.messages.outgoing;

public interface OutgoingMessageVisitor {

	public void visit(CellList message);
	public void visit(MapData message);
	public void visit(PlayerAdded message);
	public void visit(PlayerIdentity message);
	public void visit(PlayerPoints message);
	public void visit(PlayerRemoved message);
	public void visit(PongMessage message);
	public void visit(TickData message);
	public void visit(TimeRemainingMessage message);


}
