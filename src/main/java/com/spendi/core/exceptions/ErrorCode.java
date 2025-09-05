package com.spendi.core.exceptions;

import com.spendi.core.http.HttpStatusCode;

/**
 * Перечисление кодов ошибок доменного/API-уровня с привязкой к HTTP-статусу.
 *
 * <p>
 * Использование:
 * 
 * <pre>{@code
 * throw new DomainException(ErrorCode.ENTITY_NOT_FOUND, "User not found");
 * }</pre>
 */
public enum ErrorCode {
	// --- 400 BAD REQUEST ---
	UNKNOWN_PROPERTY("UNKNOWN_PROPERTY", HttpStatusCode.BAD_REQUEST),
	EMPTY_PAYLOAD("EMPTY_PAYLOAD", HttpStatusCode.BAD_REQUEST),
	MALFORMED_JSON("MALFORMED_JSON", HttpStatusCode.BAD_REQUEST),
	JSON_PARSE_ERROR("JSON_PARSE_ERROR", HttpStatusCode.BAD_REQUEST),
	JSON_MAPPING_ERROR("JSON_MAPPING_ERROR", HttpStatusCode.BAD_REQUEST),
	INVALID_QUERY_PARAMETERS("INVALID_QUERY_PARAMETERS", HttpStatusCode.BAD_REQUEST),

	// --- 401 UNAUTHORIZED ---
	UNAUTHORIZED("UNAUTHORIZED", HttpStatusCode.UNAUTHORIZED),
	TOKEN_EXPIRED("TOKEN_EXPIRED", HttpStatusCode.UNAUTHORIZED),
	TOKEN_INVALID("TOKEN_INVALID", HttpStatusCode.UNAUTHORIZED),
	INVALID_PASSWORD("INVALID_PASSWORD", HttpStatusCode.UNAUTHORIZED),

	// --- 403 FORBIDDEN ---
	ACCESS_DENIED("ACCESS_DENIED", HttpStatusCode.FORBIDDEN),
	ROLE_REQUIRED("ROLE_REQUIRED", HttpStatusCode.FORBIDDEN),

	// --- 404 NOT FOUND ---
	ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", HttpStatusCode.NOT_FOUND),
	ROUTER_NOT_FOUND("ROUTER_NOT_FOUND", HttpStatusCode.NOT_FOUND),

	// --- 409 CONFLICT ---
	ENTITY_ALREADY_EXISTS("ENTITY_ALREADY_EXISTS", HttpStatusCode.CONFLICT),
	ENTITY_NOT_DELETED("ENTITY_NOT_DELETED", HttpStatusCode.CONFLICT),
	ENTITY_ALREADY_DELETED("ENTITY_ALREADY_DELETED", HttpStatusCode.CONFLICT),

	// --- 413 PAYLOAD TOO LARGE ---
	PAYLOAD_TOO_LARGE("PAYLOAD_TOO_LARGE", HttpStatusCode.PAYLOAD_TOO_LARGE),

	// --- 415 UNSUPPORTED MEDIA TYPE ---
	UNSUPPORTED_MEDIA_TYPE("UNSUPPORTED_MEDIA_TYPE", HttpStatusCode.UNSUPPORTED_MEDIA_TYPE),

	// --- 422 UNPROCESSABLE ENTITY ---
	BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", HttpStatusCode.UNPROCESSABLE_ENTITY),
	VALIDATION_FAILED("VALIDATION_FAILED", HttpStatusCode.UNPROCESSABLE_ENTITY),

	// --- 500 INTERNAL SERVER ERROR ---
	INTERNAL_ERROR("INTERNAL_ERROR", HttpStatusCode.INTERNAL_SERVER_ERROR);

	private final String name;
	private final HttpStatusCode httpStatus;

	ErrorCode(String name, HttpStatusCode httpStatus) {
		this.name = name;
		this.httpStatus = httpStatus;
	}

	/** Человеко-читаемое имя кода ошибки (совпадает с enum-значением). */
	public String getName() {
		return name;
	}

	/** Связанный HTTP-статус. */
	public HttpStatusCode getHttpStatus() {
		return httpStatus;
	}

	/** Поиск по имени (без исключений; возвращает null если не найдено). */
	public static ErrorCode fromName(String name) {
		if (name == null)
			return null;
		for (ErrorCode ec : values()) {
			if (ec.name.equals(name))
				return ec;
		}
		return null;
	}

	@Override
	public String toString() {
		return name + " (" + httpStatus.getCode() + ")";
	}
}