/**
 * @file DomainException.java
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
 * ! my imports
 */
import com.spendi.core.http.HttpStatusCode;
import com.spendi.core.response.ApiErrorResponse;

/**
 * Базовый абстрактный класс для всех пользовательских (кастомных) исключений.
 *
 * <p>
 * Расширяет {@link RuntimeException} и добавляет:
 * </p>
 * <ul>
 * <li>{@link ErrorCode} — доменный код ошибки (содержит
 * {@link HttpStatusCode})</li>
 * <li>details — произвольные детали (например, входные параметры)</li>
 * <li>fieldErrors — ошибки валидации по полям (key = поле, value =
 * сообщение)</li>
 * </ul>
 *
 * <p>
 * Пример использования:
 * </p>
 * 
 * <pre>{@code
 * throw new UserNotFoundException("User not found", Map.of("id", id));
 * }</pre>
 */
public abstract class DomainException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ErrorCode errorCode;
	private final Map<String, Object> details; // произвольные детали
	private final Map<String, String> fieldErrors; // ошибки по полям

	/**
	 * Конструктор с обязательными полями.
	 *
	 * @param message   человеко-читаемое сообщение
	 * @param errorCode доменный код ошибки (несёт HttpStatus)
	 */
	protected DomainException(String message, ErrorCode errorCode) {
		this(message, errorCode, Map.of(), Map.of());
	}

	/**
	 * Полный конструктор.
	 *
	 * @param message     человеко-читаемое сообщение
	 * @param errorCode   доменный код ошибки
	 * @param details     дополнительные детали (null → Map.of())
	 * @param fieldErrors ошибки валидации по полям (null → Map.of())
	 */
	protected DomainException(String message,
			ErrorCode errorCode,
			Map<String, Object> details,
			Map<String, String> fieldErrors) {
		super(message);
		this.errorCode = errorCode;
		this.details = (details == null) ? Map.of() : Map.copyOf(details);
		this.fieldErrors = (fieldErrors == null) ? Map.of() : Map.copyOf(fieldErrors);
	}

	/** Доменный код ошибки. */
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	/** Соответствующий HTTP-статус (enum). */
	public HttpStatusCode getHttpStatus() {
		return errorCode.getHttpStatus();
	}

	/** Числовой HTTP-код (например, 400, 404, 500). */
	public int getStatus() {
		return getHttpStatus().getCode();
	}

	/** Имя доменного кода ошибки (например, "ENTITY_NOT_FOUND"). */
	public String getErrorCodeName() {
		return errorCode.getName();
	}

	/** Дополнительные детали (неизменяемая Map). */
	public Map<String, Object> getDetails() {
		return details;
	}

	/** Ошибки валидации по полям (неизменяемая Map). */
	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

	/**
	 * Построить стандартный {@link ApiErrorResponse} для отдачи из HTTP-слоя.
	 * HTTP-статус берётся из {@link ErrorCode}.
	 *
	 * @param requestId идентификатор запроса (из middleware)
	 * @return экземпляр {@link ApiErrorResponse}
	 */
	public ApiErrorResponse toErrorResponse(String requestId) {
		return new ApiErrorResponse(
				errorCode,
				requestId,
				getMessage(),
				fieldErrors,
				details);
	}
}