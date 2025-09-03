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
 * ! my imports
 */
import com.spendi.core.base.BaseConfig;

public class ServerConfig extends BaseConfig {
	private final String host;
	private final int port;

	public ServerConfig() {
		this.host = getenv(this.dotenv, "SPENDI_SERVER_HOST", "0.0.0.0");
		this.port = Integer.parseInt(getenv(dotenv, "SPENDI_SERVER_PORT", "6070"));
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