package soze.multilife.metrics.repository;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.time.Instant;
import java.util.*;

/**
 * A MongoDB based repository for storing/retrieving metrics related data.
 */
public class MongoMetricsRepository implements MetricsRepository {

	private final MongoDatabase db;

	public MongoMetricsRepository(MongoDatabase db) {
		this.db = Objects.requireNonNull(db);
	}

	public void saveOutgoingKilobytesPerSecond(double kbs, long timestamp) {
		MongoCollection<Document> kbsCollection = db.getCollection("outgoing_kbs");
		Document document = new Document("_id", UUID.randomUUID().toString())
			.append("kbs", kbs)
			.append("timestamp", timestamp);
		kbsCollection.insertOne(document);
	}

	public void saveIncomingKilobytesPerSecond(double kbs, long timestamp) {
		MongoCollection<Document> kbsCollection = db.getCollection("incoming_kbs");
		Document document = new Document("_id", UUID.randomUUID().toString())
				.append("kbs", kbs)
				.append("timestamp", timestamp);
		kbsCollection.insertOne(document);
	}

	public void saveMaxPlayers(int players, long timestamp) {
		MongoCollection<Document> kbsCollection = db.getCollection("players");
		Document document = new Document("_id", UUID.randomUUID().toString())
			.append("players", players)
			.append("timestamp", timestamp);
		kbsCollection.insertOne(document);
	}

	@Override
	public Map<Long, Double> getAverageKbsOutgoingSince(Instant timeSince) {
		MongoCollection<Document> kbsCollection = db.getCollection("outgoing_kbs");

		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("timestamp", new BasicDBObject("$gt", timeSince.toEpochMilli()));
		FindIterable<Document> iterable = kbsCollection.find(whereQuery);
		Iterator<Document> it = iterable.iterator();

		Map<Long, Double> averageKbs = new HashMap<>();
		it.forEachRemaining(document -> {
			averageKbs.put(document.getLong("timestamp"), document.getDouble("kbs"));
		});
		return averageKbs;
	}

	@Override
	public Map<Long, Double> getAverageKbsIncomingSince(Instant timeSince) {
		MongoCollection<Document> kbsCollection = db.getCollection("incoming_kbs");

		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put("timestamp", new BasicDBObject("$gt", timeSince.toEpochMilli()));
		FindIterable<Document> iterable = kbsCollection.find(whereQuery);
		Iterator<Document> it = iterable.iterator();

		Map<Long, Double> averageKbs = new HashMap<>();
		it.forEachRemaining(document -> {
			averageKbs.put(document.getLong("timestamp"), document.getDouble("kbs"));
		});
		return averageKbs;
	}
}
