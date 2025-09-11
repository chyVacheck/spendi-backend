
/**
 * @file apiConfig.java
 * @module config
 *
 * @author Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseConfig;

public class ApiConfig extends BaseConfig {
	private static final ApiConfig INSTANCE = new ApiConfig();
	private final String apiPrefix;

	private ApiConfig() {
		this.apiPrefix = this.getenv(this.dotenv, "SPENDI_API_PREFIX", "/api/v1");
	}

	public static ApiConfig getConfig() {
		return INSTANCE;
	}

	public String getApiPrefix() {
		return this.apiPrefix;
	}

	@Override
	public String toString() {
		return "ApiConfig{prefix='%s'}".formatted(apiPrefix);
	}
}
