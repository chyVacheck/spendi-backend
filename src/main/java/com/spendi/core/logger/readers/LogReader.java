/**
 * @file LogReader.java
 * @module core.logger.readers
 *
 * @description
 * Утилита для чтения NDJSON-логов, разбитых по датам/часам:
 * logs/YYYY-MM-DD/HH/NN.json
 *
 * Читает построчно, пропуская пустые строки; парсит каждую строку в CompressedLog.
 * Ошибочные строки логируются в stderr и пропускаются.
 *
 * Пример структуры:
 * logs/
 *   └── 2025-08-17/
 *       ├── 10/
 *       │   ├── 01.json
 *       │   └── 02.json
 *       └── 11/
 *           └── 01.json
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.logger.readers;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;

/**
 * ! java imports
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.config.LoggerConfig;
import com.spendi.core.base.CoreClass;
import com.spendi.core.logger.model.CompressedLog;
import com.spendi.core.types.EClassType;

public final class LogReader extends CoreClass {
	private static final LoggerConfig CONFIG = LoggerConfig.DEFAULT;
	private static final LogReader INSTANCE = new LogReader();

	private final ObjectMapper mapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private LogReader() {
		super(EClassType.SYSTEM, LogReader.class.getSimpleName());
	}

	public static LogReader getInstance() {
		return INSTANCE;
	}

	/**
	 * Получает все логи за указанную дату (формат YYYY-MM-DD).
	 * 
	 * @param dateStrict YYYY-MM-DD
	 * @return список CompressedLog
	 */
	public List<CompressedLog> getLogsByDate(String dateStrict) {
		List<String> files = getLogFilesForDate(dateStrict);
		return readLogsFromFiles(files);
	}

	/**
	 * Находит все .json файлы по пути logs/YYYY-MM-DD/HH/*.json,
	 * сортируя сначала по часу, потом по имени файла.
	 */
	private List<String> getLogFilesForDate(String dateStrict) {
		String basePath = CONFIG.file().path();
		File dateDir = new File(basePath, dateStrict);
		if (!dateDir.exists() || !dateDir.isDirectory()) {
			return Collections.emptyList();
		}

		// директории часов
		File[] hourDirs = dateDir.listFiles(File::isDirectory);
		if (hourDirs == null || hourDirs.length == 0) {
			return Collections.emptyList();
		}

		// сортируем часы как строки "00".."23"
		Arrays.sort(hourDirs, Comparator.comparing(File::getName));

		List<String> result = new ArrayList<>();
		for (File hourDir : hourDirs) {
			File[] jsons = hourDir.listFiles((dir, name) -> name.endsWith(".json"));
			if (jsons == null || jsons.length == 0)
				continue;

			// сортировка файлов "01.json", "02.json", ...
			Arrays.sort(jsons, Comparator.comparing(File::getName));
			for (File f : jsons) {
				result.add(f.getAbsolutePath());
			}
		}
		return result;
	}

	/**
	 * Читает NDJSON из списка файлов; каждая строка — отдельный CompressedLog.
	 * Пустые строки пропускаем; ошибки парсинга не валят процесс.
	 */
	private List<CompressedLog> readLogsFromFiles(List<String> filePaths) {
		List<CompressedLog> all = new ArrayList<>();

		for (String filePath : filePaths) {
			try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (line.isBlank())
						continue;

					try {
						CompressedLog log = mapper.readValue(line, CompressedLog.class);
						all.add(log);
					} catch (Exception parseEx) {
						System.err.println("[LogReader] Parse error in " + filePath + ": " + parseEx.getMessage());
					}
				}
			} catch (IOException ioEx) {
				System.err.println("[LogReader] Read error for " + filePath + ": " + ioEx.getMessage());
			}
		}
		return all;
	}
}
