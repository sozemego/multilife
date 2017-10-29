package soze.multilife.messages.incoming;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Optional;

public class IncomingMessageConverter {

  private static final PingMessage PING_MESSAGE = new PingMessage();

  public static Optional<IncomingMessage> convert(byte[] payload) {
    if (payload.length == 0) {
      throw new IllegalArgumentException("Outgoing byte array message cannot have 0 length.");
    }
    final byte firstByte = payload[0];
    try {
      switch (firstByte) {
        case 1:
          return Optional.of(convertClickMessage(payload));
        case 3:
          return Optional.of(convertPingMessage(payload));
      }
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }

    return Optional.empty();
  }

  private static ClickMessage convertClickMessage(byte[] payload) {
    ClickMessage message = new ClickMessage();
    int[] indices = convertPayloadToIntArray(getByteArrayWithoutTypeMarker(payload, 4));
    message.setIndices(indices);
    return message;
  }

  private static int[] convertPayloadToIntArray(byte[] payload) {
    int intNumber = payload.length / 4;

    int[] integers = new int[intNumber];
    ByteBuffer buffer = ByteBuffer.wrap(reverseArray(payload));
    for (int i = 0; i < integers.length; i++) {
      int index = buffer.getInt();
      integers[i] = index;
    }

    return integers;
  }

  private static String getString(byte[] payload) {
    try {
      return new String(payload, "UTF-16");
    } catch (UnsupportedEncodingException e) {

    }
    throw new IllegalStateException("Invalid payload");
  }

  private static String getString(byte[] payload, int start, int length) {
    byte[] strArray = copyArray(payload, start, length);
    return getString(strArray);
  }

  private static byte[] getByteArrayWithoutTypeMarker(byte[] payload, int markerLength) {
    byte[] message = new byte[payload.length - markerLength];
    System.arraycopy(payload, markerLength, message, 0, payload.length - markerLength);
    return message;
  }

  private static byte[] copyArray(byte[] from, int srcPos, int length) {
    byte[] to = new byte[length];
    System.arraycopy(from, srcPos, to, 0, length);
    return to;
  }

  private static byte[] reverseArray(byte[] arr) {
    byte[] result = new byte[arr.length];
    for (int i = 0; i < arr.length; i++) {
      result[arr.length - i - 1] = arr[i];
    }
    return result;
  }

  private static PingMessage convertPingMessage(byte[] payload) {
    return PING_MESSAGE;
  }

}
