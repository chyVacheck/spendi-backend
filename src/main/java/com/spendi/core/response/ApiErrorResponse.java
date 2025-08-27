
/** 
 * @file ApiErrorResponse.java
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
import com.spendi.core.exceptions.ErrorCode;

/**
 * Ошибочный ответ API (4xx/5xx).
 * Код HTTP берётся из связанного с ErrorCode статуса.
 */
public class ApiErrorResponse extends InnerResponse {

	private final ErrorCode errorCode;
	private final Map<String, String> errors; // field-level ошибки (может быть пустой)

	/**
	 * @param errorCode доменный код ошибки (несёт HttpStatus внутри)
	 * @param requestId id запроса
	 * @param message   сообщение для клиента
	 * @param errors    ошибки по полям (может быть null → Map.of())
	 * @param details   произвольные детали (может быть null → Map.of())
	 */
	public ApiErrorResponse(ErrorCode errorCode,
			String requestId,
			String message,
			Map<String, String> errors,
			Map<String, Object> details) {
		super(errorCode.getHttpStatus(), requestId, message, details);
		this.errorCode = errorCode;
		this.errors = (errors == null) ? Map.of() : Map.copyOf(errors);
		// Опционально: запретить успех-коды здесь
		HttpStatusCode st = errorCode.getHttpStatus();
		if (st.isSuccess()) {
			throw new IllegalArgumentException(
					"ApiErrorResponse cannot use success HTTP status: " + st);
		}
	}

	/** Доменный код ошибки. */
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	/** Ошибки валидации по полям. */
	public Map<String, String> getErrors() {
		return errors;
	}

	@Override
	public Map<String, Object> toMap() {
		return Map.of(
				"errorCode", errorCode.getName(),
				"requestId", getRequestId(),
				"status", getStatus().getName(),
				"statusCode", getStatusCode(),
				"message", getMessage(),
				"details", getDetails(),
				"errors", getErrors());
	}

	// Быстрая фабрика:
	public static ApiErrorResponse of(ErrorCode code, String requestId, String message) {
		return new ApiErrorResponse(code, requestId, message, Map.of(), Map.of());
	}
}