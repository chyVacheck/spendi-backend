
/**
 * @file JsonMappingException.java
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
 * Исключение «ошибка маппинга JSON → DTO».
 *
 * <p>
 * Используется, когда структура JSON не соответствует ожидаемой модели
 * (несовпадение типов/полей).
 * Маппится на {@link ErrorCode#JSON_MAPPING_ERROR}.
 * </p>
 *
 * Примеры:
 * 
 * <pre>{@code
 * throw new JsonMappingException("Invalid field type", Map.of("field", "data.email"));
 * }</pre>
 */
public class JsonMappingException extends DomainException {

	/**
	 * Полный конструктор.
	 *
	 * @param message человеко-читаемое сообщение
	 * @param details произвольные дополнительные детали (например, путь до поля)
	 */
	public JsonMappingException(String message, Map<String, Object> details) {
		super(message, ErrorCode.JSON_MAPPING_ERROR, details, Map.of());
	}

	/**
	 * Упрощённый вариант: только сообщение.
	 */
	public JsonMappingException(String message) {
		this(message, Map.of());
	}

	/**
	 * Упрощённый вариант: только детали (с дефолтным сообщением).
	 */
	public JsonMappingException(Map<String, Object> details) {
		this("JSON mapping error", details);
	}
}
