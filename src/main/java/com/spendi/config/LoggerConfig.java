
/**
 * @file LoggerConfig.java
 * @module config
 * @description
 * Конфигурация логгера. Берёт значения из .env / System.getenv через BaseConfig.
 * Значения по умолчанию подобраны для удобства разработки.
 *
 * Переменные окружения (пример):
 *  - SPENDI_LOG_LEVEL=INFO|DEBUG|WARN|ERROR|FATAL
 *  - SPENDI_LOG_FILE_ENABLED=true
 *  - SPENDI_LOG_FILE_MAX_SIZE=1048576
 *  - SPENDI_LOG_PATH=storage/logs
 *  - SPENDI_LOG_WIDTH_LEVEL=5
 *  - SPENDI_LOG_WIDTH_TIME=12
 *  - SPENDI_LOG_WIDTH_TYPE=12
 *  - SPENDI_LOG_WIDTH_NAME=26
 *
 * @author Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseConfig;
import com.spendi.core.logger.types.ELogLevel;

public class LoggerConfig extends BaseConfig {
	private static final LoggerConfig INSTANCE = new LoggerConfig();

	public static record FileConfig(boolean enabled, long maxSize, String path) {
	}

	private final FileConfig file;
	private final ELogLevel minLogLevel;
	private final int maxLevelWidth;
	private final int maxCurrentTimeWidth;
	private final int maxClassTypeWidth;
	private final int maxClassNameWidth;

	private LoggerConfig() {
		// file settings
		boolean enabled = parseBool(getenv(dotenv, "SPENDI_LOG_FILE_ENABLED", "true"), true);
		long maxSize = parseLong(getenv(dotenv, "SPENDI_LOG_FILE_MAX_SIZE", "1048576"), 1048576);
		String path = getenv(dotenv, "SPENDI_LOG_PATH", "storage/logs");
		this.file = new FileConfig(enabled, maxSize, path);

		// level and formatting widths
		this.minLogLevel = parseEnum(getenv(dotenv, "SPENDI_LOG_LEVEL", "INFO"), ELogLevel.INFO);
		this.maxLevelWidth = Integer.parseInt(getenv(dotenv, "SPENDI_LOG_WIDTH_LEVEL", "5"));
		this.maxCurrentTimeWidth = Integer.parseInt(getenv(dotenv, "SPENDI_LOG_WIDTH_TIME", "12"));
		this.maxClassTypeWidth = Integer.parseInt(getenv(dotenv, "SPENDI_LOG_WIDTH_TYPE", "12"));
		this.maxClassNameWidth = Integer.parseInt(getenv(dotenv, "SPENDI_LOG_WIDTH_NAME", "26"));
	}

	public static LoggerConfig getConfig() {
		return INSTANCE;
	}

	// --- API совместимый с прежним кодом ---

	public FileConfig file() {
		return file;
	}

	public FileConfig getFileConfig() {
		return file;
	}

	public ELogLevel getMinLogLevel() {
		return minLogLevel;
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

	@Override
	public String toString() {
		return "LoggerConfig{file.enabled=%s,file.maxSize=%d,file.path='%s',level=%s}".formatted(
				file.enabled(), file.maxSize(), file.path(), minLogLevel.name());
	}
}
