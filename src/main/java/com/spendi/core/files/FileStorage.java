/**
 * @file FileStorage.java
 * @module core/files
 *
 * Сервис сохранения временных файлов в постоянное хранилище (локальную директорию).
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.files;

/**
 * ! java imports
 */
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseClass;
import com.spendi.core.types.EClassType;
import com.spendi.core.utils.FileUtils;
import com.spendi.config.FileStorageConfig;

public class FileStorage extends BaseClass {
	private final Path baseDir;
	private static volatile FileStorage INSTANCE;

	/**
	 * Создать FileStorage, читая путь из .env (spendi_FILES_DIR) через
	 * FileStorageConfig.
	 */
	public FileStorage() {
		super(EClassType.SYSTEM, FileStorage.class.getSimpleName());
        FileStorageConfig cfg = FileStorageConfig.getConfig();
		// Важно: используем абсолютный путь, чтобы не зависеть от текущей рабочей
		// директории процесса
		this.baseDir = Paths.get(cfg.getBaseDir()).toAbsolutePath().normalize();
		FileUtils.createDir(this.baseDir.toString());
	}

	/**
	 * Явная инициализация синглтона (из AppInitializer).
	 */
	public static void init(FileStorage storage) {
		synchronized (FileStorage.class) {
			if (INSTANCE == null) {
				INSTANCE = storage;
			}
		}
	}

	/**
	 * Singleton-доступ к FileStorage (после init).
	 */
	public static FileStorage getInstance() {
		FileStorage ref = INSTANCE;
		if (ref == null) {
			throw new IllegalStateException("FileStorage not initialized. Call AppInitializer.initAll() in App.main");
		}
		return ref;
	}

	/** Сохранить один файл с заранее заданным id (hex). */
	public StoredFile save(String requestId, String idHex, UploadedFile f) {
		String ext = FileUtils.getFileExtension(f.getOriginalName());
		String name = idHex + (ext == null ? ".bin" : ext);

		// Дата-каталог: yyyy/MM/dd
		LocalDate today = LocalDate.now();
		Path rel = Paths.get(String.valueOf(today.getYear()),
				String.format("%02d", today.getMonthValue()),
				String.format("%02d", today.getDayOfMonth()),
				name);
		Path dst = baseDir.resolve(rel).normalize();

		// Перенос
		FileUtils.createDirFromPath(dst.toString());
		FileUtils.moveFile(f.getTempPath().toString(), dst.toString());

		this.info("File " + name + " saved", requestId, detailsOf("name", name), true);

		return new StoredFile(
				name,
				baseDir.relativize(dst).toString());
	}

	/**
	 * Проверка существования файла в хранилище (по относительному пути).
	 */
	public boolean exists(String relativePath) {
		if (relativePath == null)
			return false;
		Path p = resolve(relativePath);
		return Files.exists(p);
	}

	/** Прочитать файл по относительному пути. */
	public byte[] read(String requestId, String relativePath) {
		try {
			Path p = resolve(relativePath);
			byte[] bytes = Files.readAllBytes(p);
			this.debug("read file", requestId, detailsOf("relative", relativePath), false);
			return bytes;
		} catch (Exception e) {
			this.error("failed to read file", requestId, detailsOf("relative", relativePath, "err", e.getMessage()),
					true);
			throw new RuntimeException("Failed to read file: " + relativePath, e);
		}
	}

	public Path getBaseDir() {
		return baseDir;
	}

	/**
	 * Удалить файл из локального хранилища по относительному пути.
	 * Возвращает true, если файл существовал и был удалён.
	 */
	public boolean delete(String requestId, String relativePath) {
		try {
			Path p = resolve(relativePath);
			boolean ok = Files.deleteIfExists(p);
			if (ok) {
				this.info("file deleted", requestId, detailsOf("relative", relativePath), true);
			} else {
				this.warn("file not found for delete", requestId, detailsOf("relative", relativePath), true);
			}
			return ok;
		} catch (Exception e) {
			this.error("failed to delete file", requestId, detailsOf("relative", relativePath, "err", e.getMessage()),
					true);
			throw new RuntimeException("Failed to delete file: " + relativePath, e);
		}
	}

	/**
	 * Привести относительный путь к абсолютному внутри baseDir, с нормализацией.
	 */
	private Path resolve(String relative) {
		Path p = baseDir.resolve(relative).normalize();
		return p;
	}
}
