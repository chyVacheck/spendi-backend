
/**
 * @file ServerConfig.java
 * @module config
 * @description
 * Конфигурация сервера.
 * Берёт значения из .env / System.getenv через BaseConfig.
 * Значения по умолчанию подобраны для удобства разработки.
 * Переменные окружения (пример):
 * - SPENDI_SERVER_HOST=0.0.0.0
 * - SPENDI_SERVER_PORT=6070
 *
 * @author Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseConfig;

public class ServerConfig extends BaseConfig {
	private static final ServerConfig INSTANCE = new ServerConfig();
	private final String host;
	private final int port;

	private ServerConfig() {
		this.host = getenv(this.dotenv, "SPENDI_SERVER_HOST", "0.0.0.0");
		this.port = parseInt(getenv(dotenv, "SPENDI_SERVER_PORT", "6070"), 6070);
	}

	public static ServerConfig getConfig() {
		return INSTANCE;
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
