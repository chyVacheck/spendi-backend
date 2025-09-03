/**
 * @file EntityAlreadyExistsException.java
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
 * Исключение «сущность уже существует».
 *
 * <p>
 * Используется, когда по уникальному заданному критерию (email и т.п.) ресурс
 * уже существует.
 * Маппится на {@link ErrorCode#ENTITY_ALREADY_EXISTS}.
 * </p>
 *
 * Примеры:
 * 
 * <pre>{@code
 * throw new EntityAlreadyExistsException("User", Map.of("id", userId));
 * }</pre>
 */
public class EntityAlreadyExistsException extends DomainException {

	/**
	 * Создать исключение «сущность уже существует» с произвольными деталями.
	 *
	 * @param entityName человеко-читаемое имя сущности (например, "User")
	 * @param details    детали поиска (например, {"email": "user@example.com"})
	 */
	public EntityAlreadyExistsException(String entityName, Map<String, Object> details) {
		super(entityName + " already exists", ErrorCode.ENTITY_ALREADY_EXISTS, details, Map.of());
	}

	/**
	 * Упрощённый конструктор с парой ключ-значение (например, поле и его значение).
	 */
	public EntityAlreadyExistsException(String entityName, String key, Object value) {
		this(entityName, Map.of(key, value));
	}

	/**
	 * Самый простой вариант без деталей.
	 */
	public EntityAlreadyExistsException(String entityName) {
		this(entityName, Map.of());
	}
}