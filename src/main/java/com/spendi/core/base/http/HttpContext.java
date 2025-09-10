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

/**
 * ! lib imports
 */
import com.spendi.core.files.UploadedFile;
import com.spendi.modules.session.SessionEntity;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * ! java imports
 */
import java.nio.file.Path;
import java.util.List;

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

	/**
	 * RAW тело запроса как byte[] (если было распарсено соответствующим
	 * middleware).
	 */
	byte[] getRawBody();

	/** RAW JSON тела запроса (если было распарсено соответствующим middleware). */
	JsonNode getRawJson();

	/** Валидное DTO из тела запроса. */
	<T> T getValidBody(Class<T> type);

	/** Валидные path-параметры как DTO. */
	<T> T getValidParams(Class<T> type);

	/** Валидные query-параметры как DTO. */
	<T> T getValidQuery(Class<T> type);

	/** Список загруженных файлов (если был multipart-парсинг). */
	List<UploadedFile> getFiles();

	/** Список временных файлов для последующей очистки. */
	List<Path> getTempFiles();

	/** Сущность «аутентифицированная сессия» (типизировано вызывающим кодом). */
	SessionEntity getAuthSession();

}
