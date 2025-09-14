/**
 * @file Pagination.java
 * @module com.spendi.core.types
 *
 * @description
 * Структура для описания информации о пагинации в ответах сервисов.
 *
 * <p>Используется для возврата клиенту вместе с данными списка.</p>
 *
 * <p>Примеры использования:</p>
 * <pre>{@code
 * List<User> users = userRepository.findMany(filter, page, limit);
 * long total = userRepository.count(filter);
 * int totalPages = (int) Math.ceil((double) total / limit);
 *
 * Pagination pagination = new Pagination(page, total, limit, totalPages);
 * ServiceResponse<List<User>> response = new ServiceResponse<>(
 *     ServiceProcessType.FOUNDED,
 *     users
 * );
 * response.setPagination(pagination);
 * }</pre>
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.types;

/**
 * ! lib imports
 */
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * Класс пагинации для ответа сервисов.
 *
 * @param page       текущая страница (начиная с 1)
 * @param total      общее количество элементов
 * @param limit      количество элементов на страницу
 * @param totalPages общее количество страниц
 */
public record Pagination(
		int page,
		long total,
		int limit,
		int totalPages) {

	/**
	 * Преобразует пагинацию в карту для сериализации в JSON.
	 *
	 * @return карта с данными пагинации
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public Map<String, Object> toMap() {
		return Map.of(
				"page", page,
				"total", total,
				"limit", limit,
				"totalPages", totalPages);
	}
}
