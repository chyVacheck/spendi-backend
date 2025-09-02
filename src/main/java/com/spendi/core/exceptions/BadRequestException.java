
/**
 * @file BadRequestException.java
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
 * Исключение «ошибка валидации».
 *
 * <p>
 * Используется, когда входные данные не проходят проверку бизнес-правил
 * или схемы. Маппится на {@link ErrorCode#VALIDATION_FAILED}.
 * </p>
 *
 * <p>
 * Рекомендуется передавать fieldErrors вида {"field": "error message"}.
 * </p>
 *
 * Примеры:
 * 
 * <pre>{@code
 * throw new ValidationException(
 * 		"Validation failed",
 * 		Map.of("email", "must be a valid email"),
 * 		Map.of("payloadSize", 1234));
 * }</pre>
 */
public class BadRequestException extends DomainException {

	/**
	 * Полный конструктор.
	 *
	 * @param message     человеко-читаемое сообщение (например,
	 *                    "Validation failed")
	 * @param fieldErrors ошибки по полям (key = имя поля, value = сообщение)
	 * @param details     произвольные дополнительные детали (например, raw‑payload
	 *                    size)
	 */
	public BadRequestException(String message,
			Map<String, String> fieldErrors,
			Map<String, Object> details) {
		super(message, ErrorCode.VALIDATION_FAILED, details, fieldErrors);
	}

	/**
	 * Упрощённый вариант: только fieldErrors, без деталей.
	 */
	public BadRequestException(Map<String, String> fieldErrors) {
		this("Validation failed", fieldErrors, Map.of());
	}

	/**
	 * Ещё проще: одно поле с ошибкой.
	 */
	public BadRequestException(String field, String errorMessage) {
		this(Map.of(field, errorMessage));
	}

	/**
	 * Конструктор только с сообщением.
	 */
	public BadRequestException(String message) {
		super(message, ErrorCode.VALIDATION_FAILED, Map.of(), Map.of());
	}

	/**
	 * Конструктор с сообщением и details.
	 */
	public BadRequestException(String message, Map<String, Object> details) {
		super(message, ErrorCode.VALIDATION_FAILED, details, Map.of());
	}
}
