package soze.multilife.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Static methods dealing with sending/receiving json messages.
 */
public class JsonUtils {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();

	/**
	 * Stringifies an object. If the object is a String, returns this String.
	 * @param object
	 * @return
	 * @throws JsonProcessingException
	 */
	public static String stringify(Object object) throws JsonProcessingException {
		Objects.requireNonNull(object);
		if(object instanceof String) {
			return (String)object;
		}
		return MAPPER.writeValueAsString(object);
	}

	public static <T> String stringifyCollection(Collection<T> collection, Class<T> elementType) throws JsonProcessingException {
		return MAPPER.writerFor(TYPE_FACTORY.constructCollectionType(collection.getClass(), elementType)).writeValueAsString(collection);
	}

	public static <T> T parse(String json, Class<T> clazz) throws IOException {
		return MAPPER.readValue(json, clazz);
	}

	public static <T> List<T> parseList(String json, Class<T> elementType) throws IOException {
		return MAPPER.readerFor(TYPE_FACTORY.constructCollectionType(List.class, elementType)).readValue(json);
	}

}
