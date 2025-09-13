/**
 * @file ConsoleWriter.java
 * @module core/logger/writers
 *
 * @description
 * Консольный вывод логов с форматированием, эквивалентный TS-версии.
 */

package com.spendi.core.logger.writers;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.config.LoggerConfig;
import com.spendi.core.logger.types.ELogLevel;
import com.spendi.core.logger.model.Log;
import com.spendi.core.logger.types.LogOptions;
import com.spendi.core.types.AnsiColor;
import com.spendi.core.types.EClassType;
import com.spendi.core.utils.InstantUtils;
import com.spendi.core.utils.StringUtils;

/**
 * Работает в static-стиле и читает настройки из глобального
 * LoggerConfig.getConfig().
 */
public final class ConsoleWriter {

	private static final LoggerConfig CONFIG = LoggerConfig.getConfig();
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ConsoleWriter() {
	}

	/**
	 * Перегруженный метод для обратной совместимости.
	 * Использует настройки логирования по умолчанию.
	 */
	public static void write(
			ELogLevel level,
			EClassType moduleType,
			String moduleName,
			String message,
			String requestId,
			Map<String, Object> details) {
		write(level, moduleType, moduleName, message, requestId, details, new LogOptions(true));
	}

	/**
	 * Вариант как в TS: печать по полям с настройками логирования.
	 */
	/**
	 * Записывает сообщение в консоль с учетом переданных параметров логирования.
	 *
	 * @param level      уровень логирования
	 * @param moduleType тип модуля
	 * @param moduleName имя модуля
	 * @param message    сообщение для логирования
	 * @param requestId  идентификатор запроса
	 * @param details    дополнительные детали (могут быть null)
	 * @param options    настройки логирования (если null, используются значения по
	 *                   умолчанию)
	 */
	public static void write(
			ELogLevel level,
			EClassType moduleType,
			String moduleName,
			String message,
			String requestId,
			Map<String, Object> details,
			LogOptions options) {

		// [LEVEL]
		final String paddedLevel = AnsiColor.WHITE.getCode()
				+ "["
				+ getLevelColor(level)
				+ StringUtils.padString(level.getLabel(), CONFIG.getMaxLevelWidth())
				+ AnsiColor.WHITE.getCode()
				+ "]";

		// time
		final String paddedTime = AnsiColor.GRAY.getCode()
				+ StringUtils.padString(InstantUtils.get24HourTime(), CONFIG.getMaxCurrentTimeWidth())
				+ AnsiColor.WHITE.getCode();

		// classType
		final String paddedType = AnsiColor.CYAN.getCode()
				+ StringUtils.padString(moduleType.name(), CONFIG.getMaxClassTypeWidth())
				+ AnsiColor.WHITE.getCode();

		// className
		final String paddedName = AnsiColor.CYAN.getCode()
				+ StringUtils.padString(moduleName, CONFIG.getMaxClassNameWidth())
				+ AnsiColor.WHITE.getCode();

		// base line
		String logMessage = String.format(
				"%s %s %s: %s %s%s",
				paddedLevel,
				paddedTime,
				paddedType,
				paddedName,
				getLevelColor(level),
				message) + AnsiColor.WHITE.getCode();

		// Details (если есть и включены в настройках) — новой строкой
		if (details != null && !details.isEmpty()) {
			logMessage += "\nDetails: "
					+ AnsiColor.GRAY.getCode()
					+ toJson(details)
					+ AnsiColor.WHITE.getCode();
		}

		// RequestId (если есть и включен в настройках) — новой строкой
		if (requestId != null) {
			logMessage += "\nRequestId: "
					+ AnsiColor.GRAY.getCode()
					+ requestId
					+ AnsiColor.WHITE.getCode();
		}

		System.out.println(logMessage);
	}

	/**
	 * Удобный оверлоад: печать по нашей модели Log.
	 */
	public static void write(Log log) {
		write(
				log.getLevel(),
				log.getClassType(),
				log.getClassName(),
				log.getMessage(),
				log.getRequestId(),
				log.getDetails());
	}

	private static String getLevelColor(ELogLevel level) {
		return switch (level) {
			case DEBUG -> AnsiColor.PURPLE.getCode();
			case INFO -> AnsiColor.GREEN.getCode();
			case WARN -> AnsiColor.YELLOW.getCode();
			case ERROR, FATAL -> AnsiColor.RED.getCode();
		};
	}

	private static String toJson(Map<String, Object> details) {
		try {
			return MAPPER.writeValueAsString(details);
		} catch (JsonProcessingException e) {
			// fallback на Map#toString(), если JSON-сериализация не удалась
			return details.toString();
		}
	}
}
