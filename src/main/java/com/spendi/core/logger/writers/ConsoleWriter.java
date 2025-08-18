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
import java.util.Date;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.config.LoggerConfig;
import com.spendi.core.logger.types.ELogLevel;
import com.spendi.core.logger.model.Log;
import com.spendi.core.types.AnsiColor;
import com.spendi.core.types.EClassType;
import com.spendi.core.utils.DateUtils;
import com.spendi.core.utils.StringUtils;

/**
 * Работает в static-стиле и читает настройки из глобального
 * LoggerConfig.DEFAULT.
 */
public final class ConsoleWriter {

	private static final LoggerConfig CONFIG = LoggerConfig.DEFAULT;
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ConsoleWriter() {
	}

	/**
	 * Вариант как в TS: печать по полям.
	 */
	public static void write(
			ELogLevel level,
			EClassType moduleType,
			String moduleName,
			String message,
			String requestId,
			Map<String, Object> details) {

		// [LEVEL]
		final String paddedLevel = AnsiColor.WHITE.getCode()
				+ "["
				+ getLevelColor(level)
				+ StringUtils.padString(level.getLabel(), CONFIG.getMaxLevelWidth())
				+ AnsiColor.WHITE.getCode()
				+ "]";

		// time
		final String paddedTime = AnsiColor.GRAY.getCode()
				+ StringUtils.padString(DateUtils.get24HourTime(new Date()), CONFIG.getMaxCurrentTimeWidth())
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

		// Details (если есть) — новой строкой
		if (details != null && !details.isEmpty()) {
			logMessage += "\nDetails: "
					+ AnsiColor.GRAY.getCode()
					+ toJson(details)
					+ AnsiColor.WHITE.getCode();
		}

		// RequestId (если есть) — новой строкой
		if (requestId != null && !requestId.isBlank()) {
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
