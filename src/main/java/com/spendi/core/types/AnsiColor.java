/**
 * @file AnsiColor.java
 * @module core/types
 *
 * @description
 * ANSI-коды цветов для форматирования текста в консоли.
 * Используются в логгере для выделения уровней логов, подсветки ошибок и деталей.
 *
 * @author
 * Dmytro Shakh
 */
package com.spendi.core.types;

/**
 * ANSI-коды для окрашивания текста в терминале.
 * Каждый enum представляет escape-последовательность.
 */
public enum AnsiColor {
	/** Сброс цвета */
	WHITE("\u001B[0m"),

	/** Ошибки, критические сообщения */
	RED("\u001B[31m"),

	/** Успешные операции */
	GREEN("\u001B[32m"),

	/** Предупреждения */
	YELLOW("\u001B[33m"),

	/** Ссылки, значения */
	BLUE("\u001B[34m"),

	/** Системные сообщения, имена модулей */
	CYAN("\u001B[36m"),

	/** Отладочная информация */
	PURPLE("\u001B[35m"),

	/** Временные метки, дополнительные детали */
	GRAY("\u001B[90m");

	private final String code;

	AnsiColor(String code) {
		this.code = code;
	}

	/**
	 * Получить ANSI-код цвета.
	 *
	 * @return строка с ANSI escape-кодом
	 */
	public String getCode() {
		return code;
	}
}
