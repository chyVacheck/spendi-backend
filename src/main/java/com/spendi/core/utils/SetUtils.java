/**
 * @file SetUtils.java
 * @module core/utils
 *
 * Небольшие утилиты для удобного создания Set без null'ов
 * и с сохранением порядка (LinkedHashSet).
 */

package com.spendi.core.utils;

/**
 * ! java imports
 */
import java.util.LinkedHashSet;
import java.util.Set;

public final class SetUtils {

	private SetUtils() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Построить Set из varargs, отбрасывая null-элементы.
	 */
	public static <T> Set<T> ofNonNull(@SuppressWarnings("unchecked") T... items) {
		if (items == null || items.length == 0)
			return Set.of();
		LinkedHashSet<T> s = new LinkedHashSet<>();
		for (T it : items) {
			if (it != null)
				s.add(it);
		}
		return s;
	}

	/**
	 * Построить Set из Iterable, отбрасывая null-элементы.
	 */
	public static <T> Set<T> ofNonNull(Iterable<? extends T> items) {
		if (items == null)
			return Set.of();
		LinkedHashSet<T> s = new LinkedHashSet<>();
		for (T it : items) {
			if (it != null)
				s.add(it);
		}
		return s;
	}
}
