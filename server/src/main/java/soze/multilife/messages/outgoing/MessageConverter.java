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
		List<CellData> cells = list.cells;

		for (CellData data : cells) {
			ByteBuffer buffer = ByteBuffer.allocate(cellDataSize);
			buffer.putInt(data.x);
			buffer.putInt(data.y);
			buffer.put((byte) (data.alive ? 1 : 0));
			buffer.putInt(data.ownerId);

			System.arraycopy(buffer.array(), 0, message, offset, buffer.array().length);

			offset += cellDataSize;
		}

		return message;
	}

	public static byte[] convertMessage(TickData tickData) {
		byte[] message = new byte[5];
		message[0] = OutgoingType.TICK_DATA.getTypeMarker();
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(tickData.iterations);

		System.arraycopy(buffer.array(), 0, message, 1, buffer.array().length);
		return message;
	}

}
