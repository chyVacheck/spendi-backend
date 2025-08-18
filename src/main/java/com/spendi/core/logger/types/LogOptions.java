/**
 * @file LogOptions.java
 * @module core/logger/types
 * @description
 * Опции для логирования.
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.logger.types;

/**
 * Опции для логирования.
 */
public class LogOptions {
	private final boolean save;

	public LogOptions(boolean save) {
		this.save = save;
	}

	public boolean shouldSave() {
		return save;
	}
}
