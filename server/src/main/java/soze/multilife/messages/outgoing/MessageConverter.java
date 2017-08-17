package soze.multilife.messages.outgoing;

import java.nio.ByteBuffer;
import java.util.List;

public class MessageConverter {

	public static byte[] convertMessage(PongMessage pong) {
		return new byte[] {OutgoingType.PONG.getTypeMarker()};
	}

	public static byte[] convertMessage(CellList list) {
		byte[] message = new byte[1 + list.cells.size() * 17]; //each cell takes 17 bytes of data
		message[0] = OutgoingType.CELL_LIST.getTypeMarker();

		final int cellDataSize = 17;
		int offset = 1;
		List<CellData> cells = list.cells;

		for (CellData data : cells) {
			ByteBuffer buffer = ByteBuffer.allocate(cellDataSize);
			buffer.putInt(data.x);
			buffer.putInt(data.y);
			buffer.put((byte) (data.alive ? 1 : 0));
			buffer.putLong(data.ownerId);

			System.arraycopy(buffer.array(), 0, message, offset, buffer.array().length);

			offset += cellDataSize;
		}

		return message;
	}

}
