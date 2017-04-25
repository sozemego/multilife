package soze.multilife.messages.incoming;

public class LoginMessage extends IncomingMessage {

	private String name;
	private String rule;

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		return "LoginMessage of type [" + getType() + "]. Name [" + getName() + "]";
	}

}