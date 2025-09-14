/** 
 * @file ApiSuccessResponse.java
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
 * Успешный ответ API (2xx).
 * Валидирует, что передан именно успешный статус.
 */
public class ApiSuccessResponse<T> extends InnerResponse {

	private final T data;

	/**
	 * @param status    HTTP-статус (только 2xx-3xx; фактически ожидается 2xx)
	 * @param requestId id запроса
	 * @param message   сообщение для клиента
	 * @param details   произвольные детали (может быть null)
	 * @param data      полезная нагрузка
	 * @throws IllegalArgumentException если статус не успешный
	 */
	public ApiSuccessResponse(HttpStatusCode status,
			String requestId,
			String message,
			Map<String, Object> details,
			T data) {
		super(status, requestId, message, details);
		if (!status.isSuccess()) {
			throw new IllegalArgumentException(
					"ApiSuccessResponse requires success HTTP status (2xx/3xx)");
		}
		this.data = data;
	}

	/** Полезная нагрузка ответа. */
	public T getData() {
		return data;
	}

	@Override
	public Map<String, Object> toMap() {
		return Map.of(
				"requestId", getRequestId(),
				"status", getHttpStatusName(),
				"statusCode", getStatusCode(),
				"message", getMessage(),
				"details", getDetails(),
				"data", getData());
	}

	// Быстрые фабрики, если удобно:
	public static <T> ApiSuccessResponse<T> ok(String requestId, String message, T data) {
		return new ApiSuccessResponse<>(HttpStatusCode.OK, requestId, message, Map.of(), data);
	}

	public static <T> ApiSuccessResponse<T> ok(String requestId, String message, T data, Map<String, Object> details) {
		return new ApiSuccessResponse<>(HttpStatusCode.OK, requestId, message, details, data);
	}

	public static <T> ApiSuccessResponse<T> created(String requestId, String message, T data) {
		return new ApiSuccessResponse<>(HttpStatusCode.CREATED, requestId, message, Map.of(), data);
	}
}
