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
		@JsonSubTypes.Type(value = LoginMessage.class, name = "LOGIN")
})
public abstract class IncomingMessage {

	private Type type;

	public void setType(Type type) {
		this.type = type;
	}

	public Type getType() {
		return this.type;
	}

}
