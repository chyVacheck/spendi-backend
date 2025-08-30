/**
 * @file HttpContext.java
 * @module core/base/http
 *
 * @description
 * Абстрактный контракт для работы с HTTP-запросом/ответом
 * + общие атрибуты (requestId, success, startTime).
 *
 * Разные серверные адаптеры (Javalin, Undertow и т.д.)
 * дают конкретные реализации.
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.base.http;

public interface HttpContext {

	/** Доступ к запросу. */
	HttpRequest req();

	/** Доступ к ответу. */
	HttpResponse res();

	/** Установка произвольного атрибута. */
	void setAttr(String key, Object value);

	/** Получение атрибута по ключу (raw). */
	Object getAttr(String key);

	/** Получение атрибута с приведением типа. */
	<T> T getAttr(String key, Class<T> type);

	/** Уникальный идентификатор запроса (устанавливается на старте). */
	String getRequestId();

	/** Время старта запроса (System.nanoTime). */
	long getStartNanos();

	/** Признак успеха запроса (по умолчанию true). */
	boolean isSuccess();

	/** Явно пометить запрос как успешный/неуспешный. */
	void setSuccess(boolean success);
}