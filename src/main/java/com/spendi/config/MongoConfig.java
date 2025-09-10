
/**
 * @file MongoConfig.java
 * @module config
 *
 * @author Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! java imports
 */
import java.time.Duration;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseConfig;

/** Конфиг для подключения к MongoDB. */
public final class MongoConfig extends BaseConfig {
	public final String uri; // mongodb://user:pass@host:port/db?opts
	public final String dbName; // имя базы

	public final int minPoolSize;
	public final int maxPoolSize;
	public final Duration connectTimeout;
	public final Duration socketTimeout;
	public final Duration serverSelectionTimeout;

	public MongoConfig() {
		this.uri = getenv(this.dotenv, "SPENDI_MONGO_URI", "mongodb://localhost:27017");
		this.dbName = getenv(this.dotenv, "SPENDI_MONGO_DB", "ems");

		this.minPoolSize = (int) parseLong(getenv(this.dotenv, "SPENDI_MONGO_MIN_POOL", "0"), 0);
		this.maxPoolSize = (int) parseLong(getenv(this.dotenv, "SPENDI_MONGO_MAX_POOL", "50"), 50);

		this.connectTimeout = Duration
				.ofSeconds(parseLong(getenv(this.dotenv, "SPENDI_MONGO_CONNECT_TIMEOUT_SEC", "10"), 10));
		this.socketTimeout = Duration
				.ofSeconds(parseLong(getenv(this.dotenv, "SPENDI_MONGO_SOCKET_TIMEOUT_SEC", "30"), 30));
		this.serverSelectionTimeout = Duration
				.ofSeconds(parseLong(getenv(this.dotenv, "SPENDI_MONGO_SELECTION_TIMEOUT_SEC", "5"), 5));
	}

	// getters
	public String getUri() {
		return uri;
	}

	public String getDbName() {
		return dbName;
	}

	public int getMinPoolSize() {
		return minPoolSize;
	}

	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	public Duration getConnectTimeout() {
		return connectTimeout;
	}

	public Duration getSocketTimeout() {
		return socketTimeout;
	}

	public Duration getServerSelectionTimeout() {
		return serverSelectionTimeout;
	}

	@Override
	public String toString() {
		return "MongoConfig{uri='%s', db='%s', pool=[%d..%d]}".formatted(
				uri, dbName, minPoolSize, maxPoolSize);
	}

}
