
/**
 * @file ELogLevel.java
 * @module core/logger/types
 * @description
 * Перечисление уровней логирования приложения.
 * Задаёт порядок серьёзности от INFO (наименее серьёзный) до FATAL (наиболее серьёзный).
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.logger.types;

/**
 * Перечисление уровней логирования приложения.
 * Задаёт порядок серьёзности от INFO (наименее серьёзный) до FATAL (наиболее
 * серьёзный).
 */
public enum ELogLevel {
	INFO(0, "INFO"),
	DEBUG(1, "DEBUG"),
	WARN(2, "WARN"),
	ERROR(3, "ERROR"),
	FATAL(4, "FATAL");

	private final int code;
	private final String label;

	ELogLevel(int code, String label) {
		this.code = code;
		this.label = label;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}
}
