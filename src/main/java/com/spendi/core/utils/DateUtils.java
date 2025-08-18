/**
 * @file DateUtils.java
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Утилиты для работы с датами и временем.
 * Реализован в стиле static-only (без состояния).
 */
public final class DateUtils {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter HOUR_FORMAT = DateTimeFormatter.ofPattern("HH");

	/** Приватный конструктор, чтобы предотвратить создание экземпляра */
	private DateUtils() {
	}

	/**
	 * Получить текущее время в формате "HH:mm:ss.SSS".
	 *
	 * @param date объект {@link Date}
	 * @return строка вида "13:45:30.123"
	 */
	public static String get24HourTime(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
		return formatter.format(date);
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
