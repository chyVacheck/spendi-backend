
/**
 * @file ForbiddenException.java
 * @module core/exceptions
 *
 * Исключение, когда пользователь не имеет права на выполнение операции.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Маппится на {@link ErrorCode#ACCESS_DENIED} (HTTP 403).
 */
public class ForbiddenException extends DomainException {

	public ForbiddenException() {
		super("Access is denied", ErrorCode.ACCESS_DENIED, Map.of(), Map.of());
	}

	public ForbiddenException(String message) {
		super(message, ErrorCode.ACCESS_DENIED, Map.of(), Map.of());
	}
}
