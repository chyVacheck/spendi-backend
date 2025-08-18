/**
 * @file Logger.java
 * @module core.logger
 *
 * @description
 * Централизованный статический логгер:
 * - уровни (DEBUG/INFO/WARN/ERROR/FATAL)
 * - вывод в консоль (ConsoleWriter)
 * - сохранение в файл (FileLogWriter)
 * - компрессия (CompressedLog.fromLog)
 * - чтение логов по дате (LogReader)
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.logger;

/**
 * ! java imports
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.logger.model.CompressedLog;
import com.spendi.core.logger.model.Log;
import com.spendi.core.logger.model.LogData;
import com.spendi.core.logger.readers.LogReader;
import com.spendi.core.logger.types.ELogLevel;
import com.spendi.core.logger.writers.FileLogWriter;
import com.spendi.core.logger.writers.ConsoleWriter;
import com.spendi.core.types.EClassType;

public final class Logger {

	private static final LogReader reader = LogReader.getInstance();

	private Logger() {
	}

	// ? ===================== FILE API =====================

	/** Получить все логи за дату (YYYY-MM-DD). */
	public static List<CompressedLog> getLogsByDate(String strictDate) {
		return reader.getLogsByDate(strictDate);
	}

	// ? ===================== CORE =====================

	private static void log(ELogLevel level, LogData data) {
		// 1) собрать Log-модель
		Log log = new Log(level, data);

		// 2) печать в консоль
		ConsoleWriter.write(log);

		// 3) при необходимости — сохранить в файл
		boolean shouldSave = data.getOptions() != null && data.getOptions().shouldSave();
		if (shouldSave) {
			save(log);
		}
	}

	private static void save(Log log) {
		try {
			CompressedLog compressed = CompressedLog.fromLog(log);
			FileLogWriter.getInstance().append(compressed);
		} catch (Exception e) {
			// Не уходим в рекурсию сохранения ошибок; просто печатаем в консоль
			Map<String, Object> details = new HashMap<>();
			details.put("error", e.toString());

			LogData errData = new LogData(
					"Error saving log",
					log.getRequestId(),
					details,
					/* options */ null,
					Logger.class.getSimpleName(),
					EClassType.SYSTEM);
			ConsoleWriter.write(new Log(ELogLevel.ERROR, errData));
		}
	}

	// ? ===================== PUBLIC API =====================

	public static void debug(LogData data) {
		log(ELogLevel.DEBUG, data);
	}

	public static void info(LogData data) {
		log(ELogLevel.INFO, data);
	}

	public static void warn(LogData data) {
		log(ELogLevel.WARN, data);
	}

	public static void error(LogData data) {
		log(ELogLevel.ERROR, data);
	}

	public static void fatal(LogData data) {
		log(ELogLevel.FATAL, data);
	}
}
