
/**
 * @file RateLimitException.java
 * @module core/exceptions
 *
 * Исключение, когда пользователь превысил допустимое количество запросов.
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.exceptions;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Маппится на {@link ErrorCode#TOO_MANY_REQUESTS} (HTTP 429).
 */
public class RateLimitException extends DomainException {

	public RateLimitException() {
		super("Rate limit exceeded", ErrorCode.TOO_MANY_REQUESTS, Map.of(), Map.of());
	}

	public RateLimitException(String message) {
		super(message, ErrorCode.TOO_MANY_REQUESTS, Map.of(), Map.of());
	}

	public RateLimitException(String message, Map<String, Object> details) {
		super(message, ErrorCode.TOO_MANY_REQUESTS, details, Map.of());
	}
}
