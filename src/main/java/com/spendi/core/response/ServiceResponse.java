/**
 * @file ServiceResponse.java
 * @module com.spendi.core.response
 *
 * @description
 * Обобщённый ответ сервисного слоя.
 *
 * <p>Назначение данного класса:</p>
 * <ul>
 *   <li>Инкапсулировать результат выполнения операции сервиса</li>
 *   <li>Указывать тип результата через {@link ServiceProcessType}</li>
 *   <li>Передавать данные бизнес-логики вместе с информацией о статусе</li>
 *   <li>При необходимости содержать информацию о пагинации</li>
 * </ul>
 *
 * <p>Примеры использования:</p>
 * <pre>{@code
 * User user = userRepository.findById("123")
 *     .orElseThrow(() -> new NotFoundException("User not found"));
 *
 * ServiceResponse<User> response = new ServiceResponse<>(
 *     ServiceProcessType.FOUNDED,
 *     user
 * );
 * response.setSuccessful(true);
 *
 * // Пример с пагинацией
 * List<User> users = userRepository.findMany(filter, 1, 20);
 * long total = userRepository.count(filter);
 * Pagination pagination = new Pagination(1, total, 20, (int) Math.ceil((double) total / 20));
 *
 * ServiceResponse<List<User>> pagedResponse = new ServiceResponse<>(
 *     ServiceProcessType.FOUNDED,
 *     users
 * );
 * pagedResponse.setSuccessful(true);
 * pagedResponse.setPagination(pagination);
 * }</pre>
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.response;

import java.util.List;
/**
 * ! java imports
 */
import java.util.Optional;

/**
 * ! my imports
 */
import com.spendi.core.types.Pagination;
import com.spendi.core.types.ServiceProcessType;

public class ServiceResponse<T> {
	private ServiceProcessType process;
	private boolean successful;
	private T data;
	private Optional<Pagination> pagination;

	/**
	 * ? ===========================
	 * ? ===== ServiceResponse =====
	 * ? ===========================
	 */

	/**
	 * Конструктор ответа сервиса.
	 *
	 * @param process тип процесса выполнения {@link ServiceProcessType}
	 * @param data    данные результата выполнения операции
	 */
	public ServiceResponse(ServiceProcessType process, T data) {
		this.process = process;
		this.data = data;
		this.successful = false;
		this.pagination = Optional.empty();
	}

	/**
	 * Конструктор ответа сервиса.
	 *
	 * @param process    тип процесса выполнения {@link ServiceProcessType}
	 * @param data       данные результата выполнения операции
	 * @param pagination объект пагинации (не null) {@link Pagination}
	 */
	public ServiceResponse(ServiceProcessType process, T data, Pagination pagination) {
		if (pagination == null) {
			throw new IllegalArgumentException("Pagination must not be null");
		}
		this.process = process;
		this.data = data;
		this.successful = false;
		this.pagination = Optional.of(pagination);
	}

	/**
	 * ? ==================
	 * ? ===== static =====
	 * ? ==================
	 */

	/** FOUND(ED) — ресурс(ы) найден(ы). Обычно для read. */
	public static <T> ServiceResponse<T> founded(T data) {
		var resp = new ServiceResponse<T>(ServiceProcessType.FOUNDED, data);
		resp.setSuccessful(true);
		return resp;
	}

	/** FOUNDED — ресурсы найдены. Обычно для read. */
	public static <T> ServiceResponse<T> founded(T data, Pagination pagination) {
		ServiceResponse<T> resp = ServiceResponse.founded(data);
		resp.setPagination(pagination);
		return resp;
	}

	public static <T> ServiceResponse<List<T>> foundedEmptyList() {
		var resp = new ServiceResponse<List<T>>(ServiceProcessType.FOUNDED, List.of());
		resp.setSuccessful(true);
		return resp;
	}

	/** CREATED — ресурс создан. */
	public static <T> ServiceResponse<T> created(T data) {
		var resp = new ServiceResponse<T>(ServiceProcessType.CREATED, data);
		resp.setSuccessful(true);
		return resp;
	}

	/** UPDATED — ресурс(ы) обновлён(ы). */
	public static <T> ServiceResponse<T> updated(T data) {
		var resp = new ServiceResponse<T>(ServiceProcessType.UPDATED, data);
		resp.setSuccessful(true);
		return resp;
	}

	/** COUNTED — подсчитано (0 тоже валиден). */
	public static ServiceResponse<Long> counted(long total) {
		var r = new ServiceResponse<Long>(ServiceProcessType.COUNTED, total);
		r.setSuccessful(true);
		return r;
	}

	/** RESTORED — восстановлено из удалённых. */
	public static <T> ServiceResponse<T> restored(T data) {
		var r = new ServiceResponse<T>(ServiceProcessType.RESTORED, data);
		r.setSuccessful(true);
		return r;
	}

	/** DELETED — удалено. */
	public static <T> ServiceResponse<T> deleted(T data) {
		var r = new ServiceResponse<T>(ServiceProcessType.DELETED, data);
		r.setSuccessful(true);
		return r;
	}

	/**
	 * NOTHING для write-сценариев (matched=0/modified=0) — считаем
	 * бизнес-неуспехом.
	 */
	public static <T> ServiceResponse<T> nothingWrite(T data) {
		var r = new ServiceResponse<T>(ServiceProcessType.NOTHING, data);
		r.setFailed();
		return r;
	}

	/**
	 * ? ==============================
	 * ? ===== ServiceProcessType =====
	 * ? ==============================
	 */

	/**
	 * Получить тип процесса выполнения.
	 *
	 * @return {@link ServiceProcessType} — тип процесса
	 */
	public ServiceProcessType getProcess() {
		return process;
	}

	/**
	 * Установить тип процесса выполнения.
	 *
	 * @param process новый процесс
	 */
	public void setProcess(ServiceProcessType process) {
		this.process = process;
	}

	/**
	 * ? ======================
	 * ? ===== Successful =====
	 * ? ======================
	 */

	/**
	 * Проверить, была ли операция успешной.
	 *
	 * @return true, если операция завершилась успешно
	 */
	public boolean isSuccessful() {
		return successful;
	}

	/**
	 * Установить флаг успешности операции.
	 *
	 * @param successful true, если операция успешна; false в противном случае
	 */
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}

	/**
	 * Отметить операцию как проваленную.
	 */
	public void setFailed() {
		this.successful = false;
	}

	/**
	 * ? ================
	 * ? ===== Data =====
	 * ? ================
	 */

	/**
	 * Получить данные результата выполнения.
	 *
	 * @return данные результата
	 */
	public T getData() {
		return data;
	}

	/**
	 * ? ======================
	 * ? ===== Pagination =====
	 * ? ======================
	 */

	/**
	 * Получить данные о пагинации (если есть).
	 *
	 * @return {@link Optional} с {@link Pagination}
	 */
	public Optional<Pagination> getPagination() {
		return pagination;
	}

	/**
	 * Получить объект пагинации или выбросить исключение,
	 * если пагинация отсутствует.
	 *
	 * @return объект {@link Pagination}
	 * @throws IllegalStateException если пагинация отсутствует
	 */
	public Pagination getPaginationOrThrow() {
		return pagination.orElseThrow(() -> new IllegalStateException("Pagination is not present in ServiceResponse"));
	}

	/**
	 * Установить данные о пагинации.
	 *
	 * @param pagination объект пагинации или null
	 */
	public void setPagination(Pagination pagination) {
		if (pagination == null) {
			throw new IllegalArgumentException("Pagination must not be null");
		}
		this.pagination = Optional.of(pagination);
	}

	/**
	 * Проверить, присутствует ли объект пагинации.
	 *
	 * @return true, если объект пагинации присутствует
	 */
	public boolean hasPagination() {
		return this.pagination.isPresent();
	}
}
