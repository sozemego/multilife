package soze.multilife.messages.incoming;

/**
 * Created by KJurek on 22.02.2017.
 */
public class ClickMessage extends IncomingMessage {

  public int index;

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}
