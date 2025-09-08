
/**
 * @file StringUtils.java
 * @module core/utils
 *
 * @description
 * Утилитарные функции для работы со строками.
 * Используется в логгере для выравнивания строк.
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.utils;

/**
 * ! java imports
 */
import java.util.Optional;

/**
 * Утилиты для работы со строками.
 * Реализован в стиле static-only (не поддерживает состояние).
 */
public final class StringUtils {

	/** Приватный конструктор, чтобы предотвратить создание экземпляра */
	private StringUtils() {
	}

	/**
	 * Дополняет строку пробелами справа до заданной ширины.
	 *
	 * @param str   исходная строка
	 * @param width желаемая ширина
	 * @return строка с дополнением пробелами
	 */
	public static String padString(String str, int width) {
		if (str == null)
			return " ".repeat(width);
		return String.format("%-" + width + "s", str);
	}

	/**
	 * Преобразовать Optional<String> в lowercase-строку или null.
	 * Если сам Optional == null — вернуть null.
	 */
	public static String lowerOrNull(Optional<String> opt) {
		if (opt == null)
			return null;
		return opt.map(String::toLowerCase).orElse(null);
	}

	/**
	 * Преобразовать String в lowercase-строку или null.
	 */
	public static String lowerOrNull(String s) {
		if (s == null)
			return null;
		return s.toLowerCase();
	}
}
