package soze.multilife.messages.incoming;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "type",
  visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = LoginMessage.class, name = "LOGIN"),
  @JsonSubTypes.Type(value = ClickMessage.class, name = "CLICK")
})
public abstract class IncomingMessage {

  private IncomingType type;

  public void setType(IncomingType type) {
	this.type = type;
  }

  public IncomingType getType() {
	return this.type;
  }

}
