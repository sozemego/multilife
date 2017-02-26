package soze.multilife.messages.incoming;

/**
 * Created by KJurek on 22.02.2017.
 */
public class ClickMessage extends IncomingMessage {

  public int[] indices;

  public int[] getIndices() {
	return indices;
  }

  public void setIndices(int[] indices) {
	this.indices = indices;
  }
}
