/**
 * @file Log.java
 * @module core/logger/model
 * @description
 * Полная модель лога.
 *
 * @see ELogLevel
 * @see EClassType
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.logger.model;

/**
 * ! java imports
 */
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.logger.types.ELogLevel;
import com.spendi.core.types.EClassType;

/**
 * Полная модель лога.
 */
public class Log {
	private final LocalDateTime createdAt;
	private final ELogLevel level;
	private final String className;
	private final EClassType classType;
	private final String message;
	private final String requestId;
	private final Map<String, Object> details;

	public Log(ELogLevel level, LogData data) {
		this.createdAt = data.getCreatedAt();
		this.level = level;
		this.className = data.getClassName();
		this.classType = data.getClassType();
		this.message = data.getMessage();
		this.requestId = data.getRequestId();
		this.details = data.getDetails();
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public ELogLevel getLevel() {
		return level;
	}

	public String getClassName() {
		return className;
	}

	public EClassType getClassType() {
		return classType;
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
}
