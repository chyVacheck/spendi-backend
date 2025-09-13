
/**
 * @file InstantUtils.java
 * @module core/utils
 *
 * @description
 * Утилитарные функции для работы с датами и временем.
 * Используется в логгере для форматирования времени.
 *
 * @author
 * Dmytro Shakh
 */

package com.spendi.core.utils;

/**
 * ! java imports
 */
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.bson.BsonDateTime;

/**
 * Утилиты для работы с датами и временем.
 * Реализован в стиле static-only (без состояния).
 */
public final class InstantUtils {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH");

	/** Приватный конструктор, чтобы предотвратить создание экземпляра */
	private InstantUtils() {
	}

	/**
	 * Получить текущее время в формате "HH:mm:ss.SSS".
	 *
	 * @param date объект {@link Date}
	 * @return строка вида "13:45:30.123"
	 */
	public static String get24HourTime(Instant instant) {
		return DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault()).format(instant);
	}

	/**
	 * Получить текущее время в формате "HH:mm:ss.SSS".
	 *
	 * @return строка вида "13:45:30.123"
	 */
	public static String get24HourTime() {
		return get24HourTime(Instant.now());
	}

	/**
	 * Преобразует объект из MongoDB в Instant или возвращает null.
	 *
	 * @param value объект из MongoDB (Instant или null)
	 * @return Instant или null
	 */
	public static Instant getInstantOrNull(Object value) {
		if (value instanceof Instant) {
			return (Instant) value;
		} else if (value instanceof Date) {
			return ((Date) value).toInstant();
		} else if (value instanceof BsonDateTime) {
			return Instant.ofEpochMilli(((BsonDateTime) value).getValue());
		} else if (value instanceof String) {
			try {
				return Instant.parse((String) value);
			} catch (DateTimeParseException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * Возвращает текущую дату в формате YYYY-MM-DD
	 */
	public static String getCurrentStrictDateString() {
		return LocalDate.now().format(DATE_FORMAT);
	}

	/**
	 * Возвращает текущий час в формате HH (00–23)
	 */
	public static String getCurrentHourString() {
		return LocalDateTime.now().format(HOUR_FORMAT);
	}
}
