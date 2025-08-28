/**
 * @file ServerConfig.java
 * @module config
 *
 * @description
 * Конфигурация сервера. Загружает переменные из .env (через dotenv-java)
 * или из системных переменных (System.getenv).
 *
 * Приоритет: .env > System.getenv > значения по умолчанию.
 *
 * @author Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! lib imports
 */
import io.github.cdimascio.dotenv.Dotenv;

public class ServerConfig {
	private final String host;
	private final int port;

	public ServerConfig() {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // чтобы не падало, если .env нет
				.load();

		this.host = getenv(dotenv, "SPENDI_SERVER_HOST", "0.0.0.0");
		this.port = Integer.parseInt(getenv(dotenv, "SPENDI_SERVER_PORT", "6070"));
	}

	private String getenv(Dotenv dotenv, String key, String defaultValue) {
		String val = dotenv.get(key);
		if (val == null || val.isBlank()) {
			val = System.getenv(key);
		}
		return (val != null && !val.isBlank()) ? val : defaultValue;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return "ServerConfig{host='%s', port=%d}".formatted(host, port);
	}
}