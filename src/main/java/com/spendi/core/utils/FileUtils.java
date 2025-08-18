/**
* @file FileUtils.java
* @module core.utils
*
* Утилиты для работы с файловой системой:
* - createDir / createDirFromPath
* - getFileSize
* - deleteFile
* - moveFile
*
*
* @author Dmytro Shakh
*/
package com.spendi.core.utils;

/**
 * ! java imports
 */
import java.io.IOException;
import java.nio.file.*;

/**
 * Утилиты для работы с датами и временем.
 * Реализован в стиле static-only (без состояния).
 */
public final class FileUtils {

	/** Приватный конструктор, чтобы предотвратить создание экземпляра */
	private FileUtils() {
	}

	/**
	 * Создать директорию (и родительские тоже)
	 */
	public static void createDir(String dirPath) {
		try {
			Files.createDirectories(Paths.get(dirPath));
		} catch (IOException e) {
			throw new RuntimeException("Failed to create directory: " + dirPath, e);
		}
	}

	/**
	 * Создать директорию из пути файла
	 */
	public static void createDirFromPath(String filePath) {
		Path parent = Paths.get(filePath).getParent();
		if (parent != null) {
			createDir(parent.toString());
		}
	}

	/**
	 * Получить размер файла (в байтах)
	 */
	public static long getFileSize(String filePath) {
		try {
			return Files.size(Paths.get(filePath));
		} catch (NoSuchFileException e) {
			return 0;
		} catch (IOException e) {
			throw new RuntimeException("Failed to get file size: " + filePath, e);
		}
	}

	/**
	 * Удалить файл (игнорировать, если не существует)
	 */
	public static void deleteFile(String filePath) {
		try {
			Files.deleteIfExists(Paths.get(filePath));
		} catch (IOException e) {
			throw new RuntimeException("Failed to delete file: " + filePath, e);
		}
	}

	/**
	 * Перенос файла (с fallback на copy+delete при EXDEV)
	 */
	public static void moveFile(String src, String dst) {
		try {
			createDirFromPath(dst);
			Files.move(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);
		} catch (FileSystemException e) {
			if ("EXDEV".equals(e.getReason())) {
				try {
					Files.copy(Paths.get(src), Paths.get(dst), StandardCopyOption.REPLACE_EXISTING);
					deleteFile(src);
				} catch (IOException ex) {
					throw new RuntimeException("Failed to move file (EXDEV fallback): " + src + " -> " + dst, ex);
				}
			} else {
				throw new RuntimeException("Failed to move file: " + src + " -> " + dst, e);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to move file: " + src + " -> " + dst, e);
		}
	}
}
