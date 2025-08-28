
/**
 * @file ExceptionMapper.java
 * @module core/base/server
 *
 * @see ApiErrorResponse
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.server;

/**
 * ! my imports
 */
import com.spendi.core.response.ApiErrorResponse;

/**
 * Контракт маппинга исключений в унифицированный {@link ApiErrorResponse}.
 * Адаптер сервера вызывает это при обработке ошибок.
 */
@FunctionalInterface
public interface ExceptionMapper {
	/**
	 * Преобразовать любое Throwable в прикладное Exception (или вернуть null, чтобы
	 * оставить как есть).
	 */
	Exception toDomainException(Throwable t);
}