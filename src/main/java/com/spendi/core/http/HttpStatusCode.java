/**
 * @file HttpStatusCode.java
 * @module core/http
 *
 * @description
 * Перечисление всех основных HTTP-статусов, используемых для формирования ответов API.
 *
 * <p>Группы статусов:</p>
 * <ul>
 *   <li>2xx — Успешные ответы (Success)</li>
 *   <li>4xx — Ошибки клиента (Client Error)</li>
 *   <li>5xx — Ошибки сервера (Server Error)</li>
 * </ul>
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.http;

public enum HttpStatusCode {
	// --- Success (2xx) ---
	OK(200, "OK"), // Запрос успешно выполнен
	CREATED(201, "CREATED"), // Успешно создан новый ресурс
	ACCEPTED(202, "ACCEPTED"), // Запрос принят, но еще не обработан
	NO_CONTENT(204, "NO_CONTENT"), // Запрос выполнен, но тело ответа пустое

	// --- Client Error (4xx) ---
	BAD_REQUEST(400, "BAD_REQUEST"), // Неверный запрос
	UNAUTHORIZED(401, "UNAUTHORIZED"), // Требуется аутентификация
	FORBIDDEN(403, "FORBIDDEN"), // Доступ запрещен
	NOT_FOUND(404, "NOT_FOUND"), // Ресурс не найден
	CONFLICT(409, "CONFLICT"), // Конфликт в запросе
	PAYLOAD_TOO_LARGE(413, "PAYLOAD_TOO_LARGE"), // Тело запроса слишком большое
	UNPROCESSABLE_ENTITY(422, "UNPROCESSABLE_ENTITY"), // Ошибка валидации данных
	TOO_MANY_REQUESTS(429, "TOO_MANY_REQUESTS"), // Слишком много запросов (rate-limit)

	// --- Server Error (5xx) ---
	INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR"), // Внутренняя ошибка сервера
	NOT_IMPLEMENTED(501, "NOT_IMPLEMENTED"), // Метод не поддерживается сервером
	BAD_GATEWAY(502, "BAD_GATEWAY"), // Плохой ответ от другого сервера
	SERVICE_UNAVAILABLE(503, "SERVICE_UNAVAILABLE"), // Сервер временно недоступен
	GATEWAY_TIMEOUT(504, "GATEWAY_TIMEOUT");// Таймаут при ожидании ответа

	private final int code;
	private final String name;

	HttpStatusCode(int code, String name) {
		this.code = code;
		this.name = name;
	}

	/** Получить числовой код */
	public int getCode() {
		return code;
	}

	/** Получить строковое название */
	public String getName() {
		return name;
	}

	/** Проверить, успешный ли статус (2xx–3xx) */
	public boolean isSuccess() {
		return code >= 200 && code < 400;
	}

	/** Получить статус по числовому коду */
	public static HttpStatusCode fromCode(int code) {
		for (HttpStatusCode status : values()) {
			if (status.code == code) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown HTTP status code: " + code);
	}

	@Override
	public String toString() {
		return name + " (" + code + ")";
	}
}