/**
 * @file FileLogWriter.java
 * @module core.logger.writers
 *
 * @description
 * Отвечает за файловое сохранение логов по схеме:
 * logs/YYYY-MM-DD/HH/NN.json
 * где:
 * - YYYY-MM-DD — дата
 * - HH — час в 24-часовом формате
 * - NN — номер файла за этот час
 *
 * Автоматически создаёт директории и переключается на новый файл,
 * если текущий превышает допустимый размер.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.logger.writers;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
/**
 * ! java imports
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * ! my imports
 */
import com.spendi.config.LoggerConfig;
import com.spendi.core.base.CoreClass;
import com.spendi.core.types.EClassType;
import com.spendi.core.logger.model.CompressedLog;
import com.spendi.core.utils.DateUtils;

public final class FileLogWriter extends CoreClass {
    /** Глобальный конфиг */
    private static final LoggerConfig CONFIG = LoggerConfig.getConfig();
	/** Синглтон */
	private static final FileLogWriter INSTANCE = new FileLogWriter();

	/** Текущий открытый writer (держим между записями) */
	private BufferedWriter writer;
	/** Текущий размер файла в байтах (чтобы не дергать FS на каждый append) */
	private long currentFileSize;

	/** Параметры конфигурации */
	private final long maxFileSize;
	private final String baseDir;

	/** Состояние ротации */
	private String currentDate; // YYYY-MM-DD
	private String currentHour; // HH
	private int currentFileIndex;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private FileLogWriter() {

		super(EClassType.SYSTEM, FileLogWriter.class.getSimpleName());

		this.maxFileSize = CONFIG.file().maxSize();
		this.baseDir = CONFIG.file().path();

		this.currentDate = DateUtils.getCurrentStrictDateString();
		this.currentHour = DateUtils.getCurrentHourString();
		this.currentFileIndex = 1;
		this.currentFileSize = 0;

		ensureDirectoryStructure();
		openOrReopenWriter(); // откроем writer сразу

		// Безопасное закрытие при завершении JVM
		Runtime.getRuntime().addShutdownHook(new Thread(this::closeWriterQuietly));
	}

	public static FileLogWriter getInstance() {
		return INSTANCE;
	}

	/**
	 * Добавляет лог в текущий файл. Переключается на новый, если превышен лимит.
	 */
	public synchronized void append(CompressedLog log) {

		updateStateIfNeeded();

		try {
			String line = objectMapper.writeValueAsString(log) + "\n";
			long lineSize = line.getBytes(StandardCharsets.UTF_8).length;

			// проверяем размер, если превышает — увеличиваем индекс и открываем новый файл
			if (currentFileSize + lineSize > maxFileSize) {
				currentFileIndex++;
				currentFileSize = 0;
				openNewFile(); // <-- создаём новый файл для этого часа
			}

			writer.write(line);
			writer.flush(); // можно убрать, если не нужно сразу писать

			currentFileSize += lineSize;

		} catch (IOException e) {
			throw new RuntimeException("Failed to append log info " + getCurrentLogFilePath(), e);
		}
	}

	/**
	 * Возвращает путь к текущему лог-файлу
	 */
	public String getCurrentLogFilePath() {
		String hourDir = baseDir + File.separator + currentDate + File.separator + currentHour;
		String fileName = String.format("%02d.json", currentFileIndex);
		return hourDir + File.separator + fileName;
	}

	/**
	 * Создание директории YYYY-MM-DD/HH
	 */
	private void ensureDirectoryStructure() {
		String dirPath = baseDir + File.separator + currentDate + File.separator + currentHour;
		File dir = new File(dirPath);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("❌ Failed to create log directories: " + dirPath);
			}
		}
	}

	/**
	 * Проверяет смену даты/часа и сбрасывает индексы при необходимости.
	 */
	private void updateStateIfNeeded() {
		String newDate = DateUtils.getCurrentStrictDateString();
		String newHour = DateUtils.getCurrentHourString();

		boolean dateChanged = !currentDate.equals(newDate);
		boolean hourChanged = !currentHour.equals(newHour);

		// если не было смены времени (даты или часа) — выходим
		if (!dateChanged && !hourChanged)
			return;

		currentDate = newDate;
		currentHour = newHour;
		currentFileIndex = 1;
		currentFileSize = 0;

		// закрываем старый и открываем новый файл с индексом 01.json
		try {
			openNewFile();
		} catch (IOException e) {
			throw new RuntimeException("Failed to open new log file after date/hour change", e);
		}
	}

	/**
	 * Открывает новый файл для текущей даты/часа/индекса
	 */
	private void openNewFile() throws IOException {
		if (writer != null) {
			writer.close();
		}
		ensureDirectoryStructure(); // <--- вот эта строка решает проблему
		File file = new File(getCurrentLogFilePath());
		writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8),
				16 * 1024);
		currentFileSize = file.exists() ? file.length() : 0;
	}

	/** Открывает (или пере-открывает) текущий файл согласно состоянию */
	private void openOrReopenWriter() {
		try {
			ensureDirectoryStructure();
			File file = new File(getCurrentLogFilePath());
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8),
					16 * 1024);
			currentFileSize = file.exists() ? file.length() : 0;
		} catch (IOException e) {
			throw new RuntimeException("Failed to open log file: " + getCurrentLogFilePath(), e);
		}
	}

	/** Тихо закрыть writer */
	private void closeWriterQuietly() {
		if (writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (IOException ignored) {
			} finally {
				writer = null;
			}
		}
	}
}
