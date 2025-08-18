
/**
 * @file LogOptions.java
 * @module core/logger/model
 * @description
 * Базовая структура данных для логирования.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.logger.model;

/**
 * ! java imports
 */
import java.util.HashMap;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.logger.types.LogOptions;

/**
 * Базовая структура данных для логирования.
 */
public class LogClassData {
	private final String message;
	private final String requestId;
	private final Map<String, Object> details;
	private final LogOptions options;

	public LogClassData(String message, String requestId, Map<String, Object> details, LogOptions options) {
		this.message = message;
		this.requestId = requestId;
		this.details = details != null ? details : new HashMap<>();
		this.options = options != null ? options : new LogOptions(false);
	}

	public String getMessage() {
		return message;
	}

	public String getRequestId() {
		return requestId;
	}

	public Map<String, Object> getDetails() {
		return details;
	}

	public LogOptions getOptions() {
		return options;
	}
}
