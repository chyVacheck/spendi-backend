
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
	private final String apiPrefix;

	public ApiConfig() {
		this.apiPrefix = this.getenv(this.dotenv, "SPENDI__HOST", "/api/v1");
	}

	public String getApiPrefix() {
		return this.apiPrefix;
	}
}
