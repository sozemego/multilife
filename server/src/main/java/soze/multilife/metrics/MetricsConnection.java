package soze.multilife.metrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jetty.websocket.api.Session;
import soze.multilife.events.EventBus;
import soze.multilife.messages.outgoing.OutgoingMessage;
import soze.multilife.server.connection.BaseConnection;
import soze.multilife.server.connection.Connection;
import soze.multilife.metrics.events.TypeMetricEvent;
import soze.multilife.metrics.events.SerializedMetricEvent;

/**
 * A {@link Connection} implementation which measures various
 * metrics about outgoing messages.
 */
public class MetricsConnection extends BaseConnection {

	private final EventBus eventBus;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public MetricsConnection(long id, Session session, EventBus eventBus) {
		super(id, session);
		this.eventBus = eventBus;
	}

	@Override
	public void send(OutgoingMessage message) {
		postEvent(message);
		String serializedMessage = serialize(message);
		postEvent(serializedMessage);
		super.send(message);
	}

	/**
	 * Assemblers and posts event based on this message.
	 *
	 * @param msg outgoing message
	 */
	private void postEvent(OutgoingMessage msg) {
		TypeMetricEvent event = createEvent(msg);
		eventBus.post(event);
	}

	/**
	 * Creates {@link TypeMetricEvent}.
	 *
	 * @param msg outgoing message
	 * @return event constructed from the data
	 */
	private TypeMetricEvent createEvent(OutgoingMessage msg) {
		long timeStamp = System.nanoTime();
		return new TypeMetricEvent(timeStamp, msg.getType().toString(), getId());
	}

	/**
	 * Assembles and posts an event based on this data.
	 *
	 * @param message
	 */
	private void postEvent(String message) {
		SerializedMetricEvent event = createEvent(message);
		eventBus.post(event);
	}

	/**
	 * Creates an event based on this message.
	 *
	 * @param message
	 * @return
	 */
	private SerializedMetricEvent createEvent(String message) {
		long timeStamp = System.nanoTime();
		return new SerializedMetricEvent(timeStamp, getId(), message.length() / 2);
	}

	private String serialize(OutgoingMessage message) {
		try {
			return objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}


}
