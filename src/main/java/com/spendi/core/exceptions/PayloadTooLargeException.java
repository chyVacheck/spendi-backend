/**
 * @file PayloadTooLargeException.java
 * @module core/exceptions
 *
 * Исключение для ситуаций, когда загрузка превышает лимиты
 * (размер одного файла или суммарный размер/количество файлов).
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Маппится на {@link ErrorCode#PAYLOAD_TOO_LARGE} (HTTP 413).
 */
public class PayloadTooLargeException extends DomainException {

	/**
	 * Превышен максимальный размер одного файла.
	 *
	 * @param fileName  имя файла
	 * @param sizeBytes фактический размер
	 * @param maxBytes  максимально допустимый размер
	 */
	public PayloadTooLargeException(String fileName, long sizeBytes, long maxBytes) {
		super("File is too large: " + fileName,
				ErrorCode.PAYLOAD_TOO_LARGE,
				Map.of("file", fileName, "size", sizeBytes, "max", maxBytes),
				Map.of());
	}

	/**
	 * Превышен суммарный размер загруженных файлов.
	 *
	 * @param totalBytes суммарный размер
	 * @param maxBytes   максимально допустимый суммарный размер
	 */
	public PayloadTooLargeException(long totalBytes, long maxBytes) {
		super("Total upload size exceeded",
				ErrorCode.PAYLOAD_TOO_LARGE,
				Map.of("total", totalBytes, "max", maxBytes),
				Map.of());
	}

	/**
	 * Превышено количество файлов.
	 *
	 * @param count фактическое количество
	 * @param max   максимальное количество
	 */
	public PayloadTooLargeException(int count, int max) {
		super("Too many files in upload",
				ErrorCode.PAYLOAD_TOO_LARGE,
				Map.of("count", count, "max", max),
				Map.of());
	}
}
