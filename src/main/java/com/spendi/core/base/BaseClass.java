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
* @author Dmytro Shakh
*/

package com.spendi.core.base;

/**
 * ! java imports
 */
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
	 * @param classType тип класса {@link EClassType} (например, SERVICE, REPOSITORY, CONTROLLER)
	 * @param className имя класса
	 */
	protected BaseClass(EClassType classType, String className) {
		super(classType, className);
	}

	/*
	 * === === === ВНУТРЕННИЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ === === ===
	 */

	/**
	 * Собирает {@link LogData} с учётом дефолтов и контекста класса.
	 */
	private LogData buildLogData(ELogLevel level, String message, String requestId, Map<String, Object> details,
			LogOptions options) {

		final String msg = (message == null || message.isBlank()) ? defaultMessage(level) : message;

		final Map<String, Object> safeDetails = details == null ? Collections.emptyMap() : Map.copyOf(details);

		final LogOptions safeOptions = (options == null) ? new LogOptions(false) : options;

		return new LogData(msg, requestId, safeDetails, safeOptions, this.getClassName(), this.getClassType());
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
	 * Удобный конструктор details из varargs пар ключ-значение. Пример: detailsOf("userId", id, "attempt", 3)
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
	 * === === === БАЗОВЫЙ ВЫЗОВ ПО УРОВНЮ === === ===
	 */

	private void log(ELogLevel level, String message, String requestId, Map<String, Object> details,
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
	 * === === === INFO === === ===
	 */

	/** Полная форма. */
	protected final void info(String message, String requestId, Map<String, Object> details, LogOptions options) {
		log(ELogLevel.INFO, message, requestId, details, options);
	}

	/** Полная форма + save-флаг, вместо log опций */
	protected final void info(String message, String requestId, Map<String, Object> details, boolean save) {
		log(ELogLevel.INFO, message, requestId, details, new LogOptions(save));
	}

	/** Форма, без log опций */
	protected final void info(String message, String requestId, Map<String, Object> details) {
		log(ELogLevel.INFO, message, requestId, details, null);
	}

	/*
	 * === === === DEBUG === === ===
	 */

	/** Полная форма. */
	protected final void debug(String message, String requestId, Map<String, Object> details, LogOptions options) {
		log(ELogLevel.DEBUG, message, requestId, details, options);
	}

	/** Полная форма + save-флаг, вместо log опций */
	protected final void debug(String message, String requestId, Map<String, Object> details, boolean save) {
		log(ELogLevel.DEBUG, message, requestId, details, new LogOptions(save));
	}

	/** Форма, без log опций */
	protected final void debug(String message, String requestId, Map<String, Object> details) {
		log(ELogLevel.DEBUG, message, requestId, details, null);
	}

	/** Только сообщение */
	protected final void debug(String message) {
		log(ELogLevel.DEBUG, message, null, null, null);
	}

	/*
	 * === === === WARN === === ===
	 */

	/** Полная форма. */
	protected final void warn(String message, String requestId, Map<String, Object> details, LogOptions options) {
		log(ELogLevel.WARN, message, requestId, details, options);
	}

	/** Полная форма + save-флаг, вместо log опций */
	protected final void warn(String message, String requestId, Map<String, Object> details, boolean save) {
		log(ELogLevel.WARN, message, requestId, details, new LogOptions(save));
	}

	/** Форма, без log опций */
	protected final void warn(String message, String requestId, Map<String, Object> details) {
		log(ELogLevel.WARN, message, requestId, details, null);
	}

	/*
	 * === === === ERROR === === ===
	 */

	/** Полная форма. */
	protected final void error(String message, String requestId, Map<String, Object> details, LogOptions options) {
		log(ELogLevel.ERROR, message, requestId, details, options);
	}

	/** Полная форма + save-флаг, вместо log опций */
	protected final void error(String message, String requestId, Map<String, Object> details, boolean save) {
		log(ELogLevel.ERROR, message, requestId, details, new LogOptions(save));
	}

	/** Форма, без log опций */
	protected final void error(String message, String requestId, Map<String, Object> details) {
		log(ELogLevel.ERROR, message, requestId, details, null);
	}

	/*
	 * === === === FATAL === === ===
	 */

	/** Полная форма. */
	protected final void fatal(String message, String requestId, Map<String, Object> details, LogOptions options) {
		log(ELogLevel.FATAL, message, requestId, details, options);
	}

	/** Полная форма + save-флаг, вместо log опций */
	protected final void fatal(String message, String requestId, Map<String, Object> details, boolean save) {
		log(ELogLevel.FATAL, message, requestId, details, new LogOptions(save));
	}

	/** Форма, без log опций */
	protected final void fatal(String message, String requestId, Map<String, Object> details) {
		log(ELogLevel.FATAL, message, requestId, details, null);
	}
}
