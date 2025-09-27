// com/spendi/testutil/RealMongoTest.java

package com.spendi.testutil;

/**
 * ! lib imports
 */
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RealMongoTest {

	protected MongoClient client;
	protected MongoDatabase db;

	@BeforeAll
	void connect() {
		String uri = System.getenv().getOrDefault("MONGO_URI", "mongodb://localhost:27017");
		client = MongoClients.create(uri);

		String dbName = System.getenv().getOrDefault("MONGO_DB", "spendi_test");
		db = client.getDatabase(dbName);

		// Чистим тестовую БД перед стартом
		db.drop();
	}

	@AfterAll
	void disconnect() {
		if (client != null)
			client.close();
	}
}