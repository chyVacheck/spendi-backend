/** 
 * @file InnerResponse.java
 * @module core/http
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.response;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.http.HttpStatusCode;

/**
 * Абстрактный базовый класс для всех HTTP-ответов API.
 * Содержит общие поля: requestId, статус, сообщение и дополнительные details.
 *
 * <p>
 * Обычно сериализуется в JSON через Jackson за счёт геттеров.
 * Метод {@link #toMap()} добавлен для удобства ручных ответов/логгинга.
 *
 * @see ApiSuccessResponse
 * @see ApiErrorResponse
 */
public abstract class InnerResponse {

	private final String requestId;
	private final HttpStatusCode status;
	private final String message;
	private final Map<String, Object> details; // может быть пустой Map.of()

	/**
	 * @param status    HTTP-статус ответа
	 * @param requestId идентификатор запроса (из middleware)
	 * @param message   человеко-читаемое сообщение
	 * @param details   дополнительные детали (null заменяется на Map.of())
	 */
	protected InnerResponse(HttpStatusCode status,
			String requestId,
			String message,
			Map<String, Object> details) {
		this.status = status;
		this.requestId = requestId == null ? "" : requestId;
		this.message = message == null ? "" : message;
		this.details = details == null ? Map.of() : Map.copyOf(details);
	}

	/** Строковый id запроса. */
	public String getRequestId() {
		return requestId;
	}

	/** HTTP-статус (enum). */
	public HttpStatusCode getStatus() {
		return status;
	}

	/** Числовой HTTP-код (например, 200, 404). */
	public int getStatusCode() {
		return status.getCode();
	}

	/** Имя HTTP-статуса (например, OK, NOT_FOUND). */
	public String getHttpStatusName() {
		return status.getName();
	}

	/** Сообщение для клиента. */
	public String getMessage() {
		return message;
	}

	/** Дополнительные детали ответа. */
	public Map<String, Object> getDetails() {
		return details;
	}

	/** Удобное представление для ручной сериализации/логгинга. */
	public Map<String, Object> toMap() {
		return Map.of(
				"requestId", getRequestId(),
				"status", getStatus().getName(),
				"statusCode", getStatusCode(),
				"message", getMessage(),
				"details", getDetails());
	}
}
