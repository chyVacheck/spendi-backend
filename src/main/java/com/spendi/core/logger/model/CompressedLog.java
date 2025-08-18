/**
 * @file CompressedLog.java
 * @module core/logger/model
 * @description
 * Сжатая модель лога для хранения.
 *
 * @see ELogLevel
 * @see EClassType
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.logger.model;

/**
 * ! java imports
 */
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.logger.types.ELogLevel;
import com.spendi.core.types.EClassType;

/**
 * Сжатая модель лога (например, для хранения или быстрой передачи).
 */
public record CompressedLog(
		long c, // timestamp
		String m, // message
		ELogLevel l, // level
		String mn, // module name
		EClassType mt, // module type
		String r, // request id
		Map<String, Object> d // details
) {
	/**
	 * Сжимает объект полного лога в компактную структуру.
	 */
	public static CompressedLog fromLog(Log log) {
		return new CompressedLog(
				log.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli(),
				log.getMessage(),
				log.getLevel(),
				log.getClassName(),
				log.getClassType(),
				log.getRequestId(),
				cleanLogData(log.getDetails()));
	}

	/**
	 * Удаляет пустые объекты из поля `details` для экономии места.
	 */
	private static Map<String, Object> cleanLogData(Map<String, Object> details) {
		if (details == null || details.isEmpty()) {
			return Map.of(); // immutable пустая мапа
		}

		Map<String, Object> cleaned = new HashMap<>();

		for (Map.Entry<String, Object> entry : details.entrySet()) {
			Object value = entry.getValue();
			boolean isEmptyObject = value instanceof Map<?, ?> map && map.isEmpty();

			if (!isEmptyObject) {
				cleaned.put(entry.getKey(), value);
			}
		}

		return cleaned;
	}
}
