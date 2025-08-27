/**
 * @file EntityNotFoundException.java
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
 * Исключение «сущность не найдена».
 *
 * <p>
 * Используется, когда по заданному критерию (id, фильтр и т.п.) ресурс
 * отсутствует.
 * Маппится на {@link ErrorCode#ENTITY_NOT_FOUND}.
 * </p>
 *
 * Примеры:
 * 
 * <pre>{@code
 * throw new EntityNotFoundException("User", Map.of("id", userId));
 * throw new EntityNotFoundException("Transaction", "id", txId);
 * }</pre>
 */
public class EntityNotFoundException extends DomainException {

	/**
	 * Создать исключение «не найдено» с произвольными деталями.
	 *
	 * @param entityName человеко-читаемое имя сущности (например, "User")
	 * @param details    детали поиска (например, {"id": "123"})
	 */
	public EntityNotFoundException(String entityName, Map<String, Object> details) {
		super(entityName + " not found", ErrorCode.ENTITY_NOT_FOUND, details, Map.of());
	}

	/**
	 * Упрощённый конструктор с парой ключ-значение (например, поле и его значение).
	 */
	public EntityNotFoundException(String entityName, String key, Object value) {
		this(entityName, Map.of(key, value));
	}

	/**
	 * Самый простой вариант без деталей.
	 */
	public EntityNotFoundException(String entityName) {
		this(entityName, Map.of());
	}
}