package soze.multilife.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soze.multilife.metrics.service.MetricsService;
import soze.multilife.utils.JsonUtils;
import spark.Route;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A http handler for /metrics endpoint.
 */
public class MetricsHttpHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MetricsHttpHandler.class);

	private String html;

	private final MetricsService metricsService;

	public MetricsHttpHandler(MetricsService metricsService) {
		this.metricsService = Objects.requireNonNull(metricsService);
	}

	public Route getMetricsRoute() {
		return (request, response) -> {
			response.header("Content-type", "text-html");
			return getMetricsPage();
		};
	}

	public Route getMetricsOutgoingApi() {
		return (request, response) -> {
			String days = request.queryParams("days");

			if(days == null) {
				response.status(400);
				return "Missing days query param.";
			}

			Optional<Instant> instantOptional = getInstant(days);
			if(!instantOptional.isPresent()) {
				response.status(400);
				return "Invalid days query param.";
			}

			Instant timeSince = instantOptional.get();

			Map<Long, Double> averageKbs = metricsService.getAverageKbsOutgoingSince(timeSince);
			return JsonUtils.stringify(averageKbs);
		};
	}

	public Route getMetricsIncomingApi() {
		return (request, response) -> {
			String days = request.queryParams("days");

			if(days == null) {
				response.status(400);
				return "Missing days query param.";
			}

			Optional<Instant> instantOptional = getInstant(days);
			if(!instantOptional.isPresent()) {
				response.status(400);
				return "Invalid days query param.";
			}

			Instant timeSince = instantOptional.get();

			Map<Long, Double> averageKbs = metricsService.getAverageKbsIncomingSince(timeSince);
			return JsonUtils.stringify(averageKbs);
		};
	}

	private Optional<Instant> getInstant(String daysAgo) {
		try {
			int days = Integer.parseInt(daysAgo);
			Instant startOfDay = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
			if(days == 1) {
				return Optional.of(startOfDay);
			}
			int daysToSubtract = days - 1;
			long millisToSubtract = TimeUnit.DAYS.toMillis(daysToSubtract);
			Instant startOfDaysAgo = Instant.ofEpochMilli(startOfDay.toEpochMilli() - millisToSubtract);
			return Optional.of(startOfDaysAgo);

		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	private String getMetricsPage() {
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
