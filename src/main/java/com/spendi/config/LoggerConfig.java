
/**
 * @file LoggerConfig.java
 * @module config
 * @description
 * Конфигурация логгера. Определяет поведение логирования (файл, форматирование, ширина колонок).
 *
 * @author
 * Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! my imports
 */
import com.spendi.core.logger.types.ELogLevel;

/**
 * Конфигурация логгера в виде record.
 * Использует статическую константу {@link #DEFAULT} как дефолтный набор
 * значений.
 */
public record LoggerConfig(
		FileConfig file, // настройки файлового вывода
		ELogLevel minLogLevel, // минимальный уровень логирования
		int maxLevelWidth, // ширина поля уровня лога
		int maxCurrentTimeWidth, // ширина поля даты/времени
		int maxClassTypeWidth, // ширина поля типа модуля
		int maxClassNameWidth // ширина поля имени модуля
) {

	/**
	 * Вложенный record для настроек файлового вывода.
	 */
	public record FileConfig(
			boolean enabled,
			long maxSize,
			String path) {
		/** Дефолтная конфигурация файлового вывода */
		public static final FileConfig DEFAULT = new FileConfig(
				true, // enabled
				1 * 1024 * 1024, // 1MB
				"./logs" // путь к файлам
		);
	}

	/** Дефолтная конфигурация логгера */
	public static final LoggerConfig DEFAULT = new LoggerConfig(
			FileConfig.DEFAULT, // файл
			ELogLevel.INFO, // минимальный уровень логирования
			5, // ширина уровня
			12, // ширина даты/времени
			12, // ширина типа модуля
			26 // ширина имени модуля
	);

	// --- Методы доступа к полям ---

	public FileConfig getFileConfig() {
		return file;
	}

	public int getMaxLevelWidth() {
		return maxLevelWidth;
	}

	public int getMaxCurrentTimeWidth() {
		return maxCurrentTimeWidth;
	}

	public int getMaxClassTypeWidth() {
		return maxClassTypeWidth;
	}

	public int getMaxClassNameWidth() {
		return maxClassNameWidth;
	}

	public ELogLevel getMinLogLevel() {
		return minLogLevel;
	}
}
