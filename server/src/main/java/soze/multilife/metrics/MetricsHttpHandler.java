package soze.multilife.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * A http handler for /metrics endpoint.
 */
public class MetricsHttpHandler implements Route {

	private static final Logger LOG = LoggerFactory.getLogger(MetricsHttpHandler.class);

	private String html;

	public MetricsHttpHandler() {

	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		response.header("Content-type", "text-html");
		return createResponse();
	}

	private String createResponse() {
		//if(html == null) {
		loadHtml();
		//}
		return html;
	}

	private void loadHtml() {
		try {
			//TODO MAKE THIS PATH DEPENDENT ON SOME ENVIRONMENT VARIABLE
			//TODO no, make it load from resources/public always
			List<String> lines = Files.readAllLines(Paths.get("server/src/main/resources/public/metrics.html"));
			html = lines.stream().reduce("", (prev, curr) -> prev += curr + "\n");
		} catch (IOException e) {
			LOG.error("Could not load metrics.html file. [{}]", e);
		}
	}


}
