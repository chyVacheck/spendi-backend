/**
 * @file FileStorageConfig.java
 * @module config
 *
 * Конфигурация директорий для хранения файлов:
 * - временные загрузки (temp)
 * - постоянное хранилище (base)
 * 
 * @author Dmytro Shakh
 */

package com.spendi.config;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseConfig;

public final class FileStorageConfig extends BaseConfig {
	private static final FileStorageConfig INSTANCE = new FileStorageConfig();

	/** Директория для временных загрузок. По умолчанию: ./uploads/temp */
	private final String tempDir;
	/** Базовая директория постоянного хранилища. По умолчанию: ./storage/files */
	private final String baseDir;

	private FileStorageConfig() {
		this.baseDir = getenv(this.dotenv, "SPENDI_FILES_UPLOAD_DIR", "storage/files");
		this.tempDir = getenv(this.dotenv, "SPENDI_FILES_TEMP_DIR", "uploads/temp");
	}

	public static FileStorageConfig getConfig() {
		return INSTANCE;
	}

	public String getTempDir() {
		return tempDir;
	}

	public String getBaseDir() {
		return baseDir;
	}

	@Override
	public String toString() {
		return "FileStorageConfig{baseDir='%s', tempDir='%s'}".formatted(baseDir, tempDir);
	}
}
