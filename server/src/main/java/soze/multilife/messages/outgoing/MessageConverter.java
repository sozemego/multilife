package soze.multilife.messages.outgoing;

import java.nio.ByteBuffer;
import java.util.List;

public class MessageConverter {

	public static byte[] convertMessage(PongMessage pong) {
		return new byte[] {OutgoingType.PONG.getTypeMarker()};
	}

	public static byte[] convertMessage(CellList list) {
		byte[] message = new byte[1 + list.cells.size() * 13]; //each cell takes 13 bytes of data
		message[0] = OutgoingType.CELL_LIST.getTypeMarker();

		final int cellDataSize = 13;
		int offset = 1;
		List<CellList.CellData> cells = list.cells;

		for (CellList.CellData data : cells) {
			ByteBuffer buffer = ByteBuffer.allocate(cellDataSize);
			buffer.putInt(data.x);
			buffer.putInt(data.y);
			buffer.put((byte) (data.alive ? 1 : 0));
			buffer.putInt(data.ownerId);

			copy(buffer, message, offset);
			offset += cellDataSize;
		}

		return message;
	}

	public static byte[] convertMessage(TickData tickData) {
		byte[] message = new byte[5];
		message[0] = OutgoingType.TICK_DATA.getTypeMarker();
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(tickData.iterations);

		return copy(buffer, message);
	}

	public static byte[] convertMessage(MapData mapData) {
		byte[] message = new byte[9];
		message[0] = OutgoingType.MAP_DATA.getTypeMarker();

		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putInt(mapData.width);
		buffer.putInt(mapData.height);

		return copy(buffer, message);
	}

	public static byte[] convertMessage(PlayerIdentity playerIdentity) {
		byte[] message = new byte[5];
		message[0] = OutgoingType.PLAYER_IDENTITY.getTypeMarker();

		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(playerIdentity.playerId);

		return copy(buffer, message);
	}

	public static byte[] convertMessage(TimeRemainingMessage timeRemainingMessage) {
		byte[] message = new byte[5];
		message[0] = OutgoingType.TIME_REMAINING.getTypeMarker();

		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putFloat(timeRemainingMessage.remainingTime);

		return copy(buffer, message);
	}

	public static byte[] convertMessage(PlayerAdded playerAdded) {
		final int messageLength = 9 + (playerAdded.getPlayerName().length() * 2);
		byte[] message = new byte[messageLength];
		message[0] = OutgoingType.PLAYER_ADDED.getTypeMarker();

		ByteBuffer buffer = ByteBuffer.allocate(messageLength - 1);
		buffer.putInt(playerAdded.getPlayerId());
		buffer.putInt(playerAdded.getPlayerColor());
		for(char c: playerAdded.getPlayerName().toCharArray()) {
			buffer.putChar(c);
		}

		return copy(buffer, message);
	}

	public static byte[] convertMessage(PlayerRemoved playerRemoved) {
		byte[] message = new byte[5];
		message[0] = OutgoingType.PLAYER_REMOVED.getTypeMarker();

		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(playerRemoved.getPlayerId());

		return copy(buffer, message);
	}

	public static byte[] convertMessage(PlayerPoints playerPoints) {
		byte[] message = new byte[9];
		message[0] = OutgoingType.PLAYER_POINTS.getTypeMarker();

		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putInt(playerPoints.getPlayerId());
		buffer.putInt(playerPoints.getPlayerPoints());

		return copy(buffer, message);
	}

	private static byte[] copy(ByteBuffer buffer, byte[] to) {
		return copy(buffer.array(), to, buffer.array().length);
	}

	private static byte[] copy(ByteBuffer buffer, byte[] to, int offset) {
		return copy(buffer.array(), to, offset, buffer.array().length);
	}

	private static byte[] copy(byte[] from, byte[] to, int length) {
		return copy(from, to, 1, length);
	}

	private static byte[] copy(byte[] from, byte[] to, int offset, int length) {
		System.arraycopy(from, 0, to, offset, length);
		return to;
	}

}
