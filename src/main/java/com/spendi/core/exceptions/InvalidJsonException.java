
/**
 * @file InvalidJsonException.java
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
 * Исключение «некорректный JSON» (ошибка парсинга).
 *
 * <p>
 * Используется, когда входной JSON имеет синтаксические ошибки и не может быть
 * распарсен.
 * Маппится на {@link ErrorCode#JSON_PARSE_ERROR}.
 * </p>
 *
 * Примеры:
 * 
 * <pre>{@code
 * throw new InvalidJsonException("Invalid JSON syntax", Map.of("offset", 123));
 * }</pre>
 */
public class InvalidJsonException extends DomainException {

	/**
	 * Полный конструктор.
	 *
	 * @param message человеко-читаемое сообщение
	 * @param details произвольные дополнительные детали (например, позиция ошибки)
	 */
	public InvalidJsonException(String message, Map<String, Object> details) {
		super(message, ErrorCode.JSON_PARSE_ERROR, details, Map.of());
	}

	/**
	 * Упрощённый вариант: только сообщение.
	 */
	public InvalidJsonException(String message) {
		this(message, Map.of());
	}

	/**
	 * Упрощённый вариант: только детали (с дефолтным сообщением).
	 */
	public InvalidJsonException(Map<String, Object> details) {
		this("Invalid JSON", details);
	}
}
