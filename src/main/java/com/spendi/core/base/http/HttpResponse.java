
/**
 * @file HttpResponse.java
 * @module core/base/http
 *
 * @see HttpMethod
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.http;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Фреймворк-агностичное представление HTTP-ответа.
 * Адаптер веб-сервера реализует реальную отправку.
 */
public interface HttpResponse {
	/**
	 * Установить HTTP-статус. Возвращает this для чейнинга.
	 */
	HttpResponse status(int statusCode);

	/**
	 * Установить заголовок (перезаписывает предыдущее значение).
	 */
	HttpResponse header(String name, String value);

	/**
	 * Массовая установка заголовков.
	 */
	default HttpResponse headers(Map<String, String> headers) {
		if (headers != null) {
			headers.forEach(this::header);
		}
		return this;
	}

	/**
	 * Отправить текстовый ответ. Должен завершить обработку.
	 */
	void sendText(String text);

	/**
	 * Отправить бинарный ответ. Должен завершить обработку.
	 */
	void sendBytes(byte[] bytes);

	/**
	 * Отправить JSON-ответ. Конкретный адаптер сериализует объект.
	 */
	void sendJson(Object body);
}
