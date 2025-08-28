
/**
 * @file HttpRequest.java
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ! my imports
 */
import com.spendi.core.http.HttpMethod;

/**
 * Фреймворк-агностичное представление HTTP-запроса.
 * Адаптер веб-сервера обязан пробросить реальные данные.
 */
public interface HttpRequest {
	/**
	 * Метод запроса.
	 */
	HttpMethod method();

	/**
	 * Полный путь (URI без схемы/домена). Например: /api/v1/users/123
	 */
	String path();

	/**
	 * Значение path-параметра (если есть). Например: "id" -> "123".
	 */
	Optional<String> pathParam(String name);

	/**
	 * Все path-параметры.
	 */
	Map<String, String> pathParams();

	/**
	 * Первый query-параметр по имени.
	 */
	Optional<String> queryParam(String name);

	/**
	 * Все query-параметры (name -> [values]).
	 */
	Map<String, List<String>> queryParams();

	/**
	 * Первый заголовок по имени.
	 */
	Optional<String> header(String name);

	/**
	 * Все заголовки (name -> first value).
	 */
	Map<String, String> headers();

	/**
	 * Тело запроса как строка (если применимо).
	 */
	Optional<String> bodyAsString();

	/**
	 * Тело запроса как bytes (если применимо).
	 */
	Optional<byte[]> bodyAsBytes();

	/**
	 * Удалённый адрес клиента (если доступно).
	 */
	Optional<String> remoteAddress();
}