/**
 * @file InvalidPdfException.java
 * @module core/exceptions
 *
 * Ошибки, связанные с валидацией PDF-файлов (страницы, шифрование, вложения).
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Маппится на {@link ErrorCode#VALIDATION_FAILED} (HTTP 422).
 */
public class InvalidPdfException extends DomainException {

	public InvalidPdfException(String message, Map<String, Object> details) {
		super(message, ErrorCode.VALIDATION_FAILED, details, Map.of());
	}

	public static InvalidPdfException pagesExceeded(String fileName, int pages, int maxPages) {
		return new InvalidPdfException(
				"PDF pages exceeded",
				Map.of("file", fileName, "pages", pages, "maxPages", maxPages));
	}

	public static InvalidPdfException encryptedNotAllowed(String fileName) {
		return new InvalidPdfException(
				"Encrypted PDF is not allowed",
				Map.of("file", fileName));
	}
}
