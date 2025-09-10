/**
 * @file MongoProvider.java
 * @module core/database
 *
 * Простой провайдер MongoDatabase на основе MongoConfig.
 * Добавлен явный метод init() для явной инициализации подключения.
 *
 * Потокобезопасность: используется double-checked locking на статических полях.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.database;

/**
 * ! lib imports
 */
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * ! java imports
 */
import java.util.concurrent.TimeUnit;

/**
 * ! my imports
 */
import com.spendi.config.MongoConfig;

public final class MongoProvider {
	private static volatile MongoClient client;
	private static volatile MongoDatabase database;

	private MongoProvider() {
	}

	/**
	 * Явная инициализация подключения к MongoDB.
	 * Создаёт MongoClient и MongoDatabase и сохраняет их в статических полях.
	 * Повторные вызовы — безопасны: соединение создаётся один раз.
	 */
	public static void init() {
		if (database != null)
			return;

		synchronized (MongoProvider.class) {
			if (database != null)
				return;

			MongoConfig cfg = new MongoConfig();
			ConnectionString cs = new ConnectionString(cfg.getUri());
			MongoClientSettings settings = MongoClientSettings.builder()
					.applyConnectionString(cs)
					.applyToConnectionPoolSettings(b -> b
							.minSize(cfg.getMinPoolSize())
							.maxSize(cfg.getMaxPoolSize()))
					.applyToSocketSettings(b -> b
							.connectTimeout((int) cfg.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
							.readTimeout((int) cfg.getSocketTimeout().toMillis(), TimeUnit.MILLISECONDS))
					.applyToClusterSettings(b -> b
							.serverSelectionTimeout((int) cfg.getServerSelectionTimeout().toMillis(),
									TimeUnit.MILLISECONDS))
					.build();

			client = MongoClients.create(settings);
			database = client.getDatabase(cfg.getDbName());
		}
	}

	/**
	 * Получить инстанс базы данных.
	 * Если соединение ещё не инициализировано, выполняется ленивый init().
	 */
	public static MongoDatabase getDatabase() {
		if (database == null) {
			init();
		}
		return database;
	}
}
