/**
 * @file InvalidPasswordException.java
 * @module core/exceptions
 *
 *         Ошибки, связанные с валидацией паролей.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Маппится на {@link ErrorCode#INVALID_PASSWORD} (HTTP 401).
 */
public class InvalidPasswordException extends DomainException {

	public InvalidPasswordException(String message, Map<String, Object> details,
			Map<String, String> errors) {
		super(message, ErrorCode.INVALID_PASSWORD, details, errors);
	}

	public InvalidPasswordException(String message, Map<String, Object> details) {
		super(message, ErrorCode.INVALID_PASSWORD, details, Map.of());
	}
}
