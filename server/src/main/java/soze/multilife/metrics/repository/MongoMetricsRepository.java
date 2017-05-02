package soze.multilife.metrics.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.UUID;

/**
 * A MongoDB based repository for storing/retrieving metrics related data.
 */
public class MongoMetricsRepository implements MetricsRepository {

	private final MongoDatabase db;

	public MongoMetricsRepository(MongoDatabase db) {
		this.db = db;
	}

	public void saveKilobytesPerSecond(double kbs, long timestamp) {
		MongoCollection<Document> kbsCollection = db.getCollection("kbs");
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
}
