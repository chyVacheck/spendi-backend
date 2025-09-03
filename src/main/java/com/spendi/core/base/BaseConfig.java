/**
 * @file BaseConfig.java
 * @module config
 *
 * @description
 * класс базовой конфигурация. Загружает переменные из .env (через dotenv-java)
 * или из системных переменных (System.getenv).
 *
 * Приоритет: .env > System.getenv > значения по умолчанию.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base;

/**
 * ! lib imports
 */
import io.github.cdimascio.dotenv.Dotenv;

public abstract class BaseConfig {
	protected Dotenv dotenv = Dotenv.configure()
			.ignoreIfMissing() // чтобы не падало, если .env нет
			.load();

	public BaseConfig() {
	}

	protected String getenv(Dotenv dotenv, String key, String defaultValue) {
		String val = dotenv.get(key);
		if (val == null || val.isBlank()) {
			val = System.getenv(key);
		}
		return (val != null && !val.isBlank()) ? val : defaultValue;
	}

}