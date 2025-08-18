/**
* @module core/base
* @description
* Базовый класс для всех прикладных классов системы.
* Наследуется от {@link CoreClass}, добавляет удобные методы логирования.
*
* Основные задачи:
* - Единообразный вызов логирования из сервисов/контроллеров/репозиториев
* - Дефолтные сообщения по уровню при их отсутствии
* - null-safe обработка requestId / details / options
* - Упрощённые перегрузки (минимум обязательных аргументов)
*
* Политика:
* - Логи человеко-читаемые (англ. язык)
* - Чувствительные данные в details не класть (или предварительно маскировать)
*
* @see CoreClass
* @see EClassType
* @see Logger
* @see LogData
* @see LogOptions
*
* @author
* Dmytro Shakh
*/
package com.spendi.core.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;
import com.spendi.core.logger.Logger;
import com.spendi.core.logger.model.LogData;
import com.spendi.core.logger.types.ELogLevel;
import com.spendi.core.logger.types.LogOptions;

public abstract class BaseClass extends CoreClass {

	/**
	 * Конструктор базового класса.
	 *
	 * @param classType тип класса {@link EClassType} (например, SERVICE,
	 *                  REPOSITORY, CONTROLLER)
	 * @param className имя класса
	 */
	protected BaseClass(EClassType classType, String className) {
		super(classType, className);
	}

	/*
	 * =========================
	 * ВНУТРЕННИЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
	 * =========================
	 */

	/**
	 * Собирает {@link LogData} с учётом дефолтов и контекста класса.
	 */
	private LogData buildLogData(ELogLevel level,
			String message,
			String requestId,
			Map<String, Object> details,
			LogOptions options) {

		final String msg = (message == null || message.isBlank())
				? defaultMessage(level)
				: message;

		final Map<String, Object> safeDetails = details == null ? Collections.emptyMap() : Map.copyOf(details);

		final LogOptions safeOptions = (options == null) ? new LogOptions(false) : options;

		return new LogData(
				msg,
				requestId,
				safeDetails,
				safeOptions,
				this.getClassName(),
				this.getClassType());
	}

	/**
	 * Возвращает дефолтное сообщение в зависимости от уровня.
	 */
	private String defaultMessage(ELogLevel level) {
		return switch (level) {
			case DEBUG -> "debug log";
			case INFO -> "info log";
			case WARN -> "warn log";
			case ERROR -> "error log";
			case FATAL -> "fatal log";
		};
	}

	/**
	 * Удобный конструктор details из varargs пар ключ-значение.
	 * Пример: detailsOf("userId", id, "attempt", 3)
	 *
	 * @throws IllegalArgumentException если передано нечётное число аргументов
	 */
	protected final Map<String, Object> detailsOf(Object... kv) {
		if (kv == null || kv.length == 0)
			return Collections.emptyMap();
		if ((kv.length % 2) != 0) {
			throw new IllegalArgumentException("detailsOf requires even number of arguments (key, value, ...)");
		}
		Map<String, Object> map = new HashMap<>(kv.length / 2);
		for (int i = 0; i < kv.length; i += 2) {
			Object key = kv[i];
			Object val = kv[i + 1];
			if (key == null)
				continue; // пропускаем null-ключи
			map.put(Objects.toString(key), val);
		}
		return map;
	}

	/*
	 * =========================
	 * БАЗОВЫЙ ВЫЗОВ ПО УРОВНЮ
	 * =========================
	 */

	private void log(ELogLevel level,
			String message,
			String requestId,
			Map<String, Object> details,
			LogOptions options) {

		LogData data = buildLogData(level, message, requestId, details, options);

		switch (level) {
			case DEBUG -> Logger.debug(data);
			case INFO -> Logger.info(data);
			case WARN -> Logger.warn(data);
			case ERROR -> Logger.error(data);
			case FATAL -> Logger.fatal(data);
		}
	}

	/*
	 * =========================
	 * DEBUG
	 * =========================
	 */

	/** Полная форма. */
	protected final void debug(String message, String requestId, Map<String, Object> details, LogOptions options) {
		log(ELogLevel.DEBUG, message, requestId, details, options);
	}

	/** Только сообщение. */
	protected final void debug(String message) {
		log(ELogLevel.DEBUG, message, null, null, null);
	}

	/** Сообщение + save-флаг. */
	protected final void debug(String message, boolean save) {
		log(ELogLevel.DEBUG, message, null, null, new LogOptions(save));
	}

	/** Сообщение + details. */
	protected final void debug(String message, Map<String, Object> details) {
		log(ELogLevel.DEBUG, message, null, details, null);
	}

	/** Только details (сообщение дефолтное). */
	protected final void debug(Map<String, Object> details) {
		log(ELogLevel.DEBUG, null, null, details, null);
	}

	/** Без всего (полностью дефолтное сообщение). */
	protected final void debug() {
		log(ELogLevel.DEBUG, null, null, null, null);
	}

	/*
	 * =========================
	 * WARN
	 * =========================
	 */

	protected final void warn(String message) {
		log(ELogLevel.WARN, message, null, null, null);
	}

	protected final void warn(String message, boolean save) {
		log(ELogLevel.WARN, message, null, null, new LogOptions(save));
	}

	protected final void warn(String message, Map<String, Object> details) {
		log(ELogLevel.WARN, message, null, details, null);
	}

	protected final void warn(Map<String, Object> details) {
		log(ELogLevel.WARN, null, null, details, null);
	}

	protected final void warn() {
		log(ELogLevel.WARN, null, null, null, null);
	}

	/*
	 * =========================
	 * ERROR
	 * =========================
	 */

	protected final void error(String message, String requestId, Map<String, Object> details, LogOptions options) {
		log(ELogLevel.ERROR, message, requestId, details, options);
	}

	protected final void error(String message) {
		log(ELogLevel.ERROR, message, null, null, null);
	}

	protected final void error(String message, boolean save) {
		log(ELogLevel.ERROR, message, null, null, new LogOptions(save));
	}

	protected final void error(String message, Map<String, Object> details) {
		log(ELogLevel.ERROR, message, null, details, null);
	}

	protected final void error(Map<String, Object> details) {
		log(ELogLevel.ERROR, null, null, details, null);
	}

	protected final void error() {
		log(ELogLevel.ERROR, null, null, null, null);
	}

	/*
	 * =========================
	 * FATAL
	 * =========================
	 */

	protected final void fatal(String message, String requestId, Map<String, Object> details, LogOptions options) {
		log(ELogLevel.FATAL, message, requestId, details, options);
	}

	protected final void fatal(String message) {
		log(ELogLevel.FATAL, message, null, null, null);
	}

	protected final void fatal(String message, boolean save) {
		log(ELogLevel.FATAL, message, null, null, new LogOptions(save));
	}

	protected final void fatal(String message, Map<String, Object> details) {
		log(ELogLevel.FATAL, message, null, details, null);
	}

	protected final void fatal(Map<String, Object> details) {
		log(ELogLevel.FATAL, null, null, details, null);
	}

	protected final void fatal() {
		log(ELogLevel.FATAL, null, null, null, null);
	}
}
