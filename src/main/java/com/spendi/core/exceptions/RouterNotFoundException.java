/**
 * @file RouterNotFoundException.java
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
 * Исключение «роутер не найден».
 *
 * <p>
 * Используется, когда по заданному роутеру обработчик не найден.
 * Маппится на {@link ErrorCode#ROUTER_NOT_FOUND}.
 * </p>
 *
 * Примеры:
 * 
 * <pre>{@code
 * throw new RouterNotFoundException("User");
 * throw new RouterNotFoundException("Transaction");
 * }</pre>
 */
public class RouterNotFoundException extends DomainException {

	/**
	 * Создать исключение «не найден роутер».
	 *
	 * @param router роутер (например, "/user")
	 */
	public RouterNotFoundException(String router) {
		super("Router '" + router + "' not found", ErrorCode.ROUTER_NOT_FOUND, null, Map.of());
	}

}