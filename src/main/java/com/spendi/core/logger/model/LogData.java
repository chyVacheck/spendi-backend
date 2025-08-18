/**
 * @file LogData.java
 * @module core/logger/model
 * @description
 * Базовая структура данных для логирования.
 *
 * @see LogClassData
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.logger.model;

/**
 * ! java imports
 */
import java.util.Map;
import java.time.LocalDateTime;

/**
 * ! my imports
 */
import com.spendi.core.logger.types.LogOptions;
import com.spendi.core.types.EClassType;

/**
 * Расширенные данные лога, включающие имя и тип модуля.
 */
public class LogData extends LogClassData {
	private final String className;
	private final EClassType classType;
	private final LocalDateTime createdAt;

	public LogData(String message, String requestId, Map<String, Object> details, LogOptions options,
			String className, EClassType classType) {
		super(message, requestId, details, options);
		this.className = className;
		this.classType = classType;
		this.createdAt = LocalDateTime.now();
	}

	public String getClassName() {
		return className;
	}

	public EClassType getClassType() {
		return classType;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
