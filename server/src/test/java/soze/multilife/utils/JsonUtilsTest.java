package soze.multilife.utils;

import org.junit.Test;
import soze.multilife.messages.incoming.LoginMessage;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class JsonUtilsTest {

	@Test
	public void testStringify() throws Exception {
		LoginMessage message = new LoginMessage();
		message.setName("Player");

		String json = JsonUtils.stringify(message);
		assertNotEquals(json, null);

		message = JsonUtils.parse(json, LoginMessage.class);
		assertEquals(message.getClass(), LoginMessage.class);
		assertEquals(message.getName(), "Player");
	}

	@Test
	public void testStringifyCollection() throws Exception {
		List<LoginMessage> msgs = new ArrayList<>();
		msgs.add(new LoginMessage());
		msgs.add(new LoginMessage());

		String json = JsonUtils.stringifyCollection(msgs, LoginMessage.class);

		List<LoginMessage> messages = JsonUtils.parseList(json, LoginMessage.class);
		assertEquals(messages.size(), 2);
		assertEquals(messages.get(0).getName(), null);
		assertEquals(messages.get(1).getName(), null);
	}

	@Test
	public void testStringifyString() throws Exception {
		String name = "Player";
		String json = JsonUtils.stringify(name);
		assertEquals(json, name);
	}

}