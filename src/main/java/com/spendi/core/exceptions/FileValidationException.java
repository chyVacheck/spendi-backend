/**
 * @file FileValidationException.java
 * @module core/exceptions
 *
 * Обобщённое исключение ошибок валидации загружаемых файлов
 * (неструктурные ошибки: недопустимое расширение, пустой файл и т.п.).
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
public class FileValidationException extends DomainException {

	public FileValidationException(String message) {
		super(message, ErrorCode.VALIDATION_FAILED, Map.of(), Map.of());
	}

	public FileValidationException(String message, Map<String, Object> details) {
		super(message, ErrorCode.VALIDATION_FAILED, details, Map.of());
	}

	public FileValidationException(String message, Map<String, Object> details, Map<String, String> fieldErrors) {
		super(message, ErrorCode.VALIDATION_FAILED, details, fieldErrors);
	}
}
