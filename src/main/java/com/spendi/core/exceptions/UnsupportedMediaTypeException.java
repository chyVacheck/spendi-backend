/**
 * @file UnsupportedMediaTypeException.java
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
 * Исключение «медиа тип не поддерживается».
 *
 * <p>
 * Используется, когда передан другой медиа тип, от ожидаемого
 * Маппится на {@link ErrorCode#UNSUPPORTED_MEDIA_TYPE}.
 * </p>
 *
 * Примеры:
 * 
 * <pre>{@code
 * throw new UnsupportedMediaTypeException("application/json", Map.of("supported", "image/png"));
 * }</pre>
 */
public class UnsupportedMediaTypeException extends DomainException {

	/**
	 * Создать исключение «медиа тип не поддерживается» с деталями.
	 *
	 * @param actual   входящий медиа тип (например, "application/json")
	 * @param expected ожидаемый медиа тип (например, "image/png")
	 */
	public UnsupportedMediaTypeException(String actual, String expected) {
		super("Unsupported media type: " + actual, ErrorCode.UNSUPPORTED_MEDIA_TYPE,
				Map.of("actual", actual, "expected", expected),
				Map.of());
	}

	public UnsupportedMediaTypeException(String actual, Map<String, Object> details) {
		super("Unsupported media type: " + actual,
				ErrorCode.UNSUPPORTED_MEDIA_TYPE,
				details != null ? details : Map.of(),
				Map.of());
	}

}