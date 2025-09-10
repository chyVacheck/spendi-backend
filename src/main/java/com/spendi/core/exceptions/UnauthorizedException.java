/**
 * @file UnauthorizedException.java
 * @module core/exceptions
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * 401 UNAUTHORIZED — требуется аутентификация или она недействительна.
 */
public class UnauthorizedException extends DomainException {

	public UnauthorizedException(String message) {
		super(message, ErrorCode.UNAUTHORIZED, Map.of(), Map.of());
	}

	public UnauthorizedException(String message, Map<String, Object> details) {
		super(message, ErrorCode.UNAUTHORIZED, details, Map.of());
	}
}
