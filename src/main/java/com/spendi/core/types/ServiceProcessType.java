/**
 * @file EClassType.java
 * @module com.spendi.core.types
 * @description
 * Перечисление всех возможных типов ответа сервиса в системе.
 *
 * <p>Назначение данного перечисления:</p>
 * <ul>
 *   <li>Определяет тип ответа, каждого запроса к сервису</li>
 * 	 <li>Применяется при формировании {@link com.spendi.core.response.ServiceResponse}</li>
 *   <li>Используется для логирования, мониторинга и отладки</li>
 * </ul>
 *
 * <p>Примеры использования:</p>
 * <pre>{@code
 * ServiceResponse<User> response = new ServiceResponse<>(
 *     ServiceProcessType.FOUNDED,
 *     user
 * );
 *
 * if (response.getProcess() == ServiceProcessType.FOUNDED) {
 *     System.out.println("User was found!");
 * }
 * }</pre>
 *
 * <p>Типы ответов сервиса:</p>
 * <ul>
 *   <li>{@link #FOUNDED} — Ресурс успешно найден</li>
 *   <li>{@link #CREATED} — Ресурс успешно создан</li>
 *   <li>{@link #UPDATED} — Ресурс успешно обновлён</li>
 *   <li>{@link #COUNTED} — Ресурс успешно посчитан</li>
 *   <li>{@link #RESTORED} — Ресурс успешно восстановлен из удаленных</li>
 *   <li>{@link #DELETED} — Ресурс успешно удалён</li>
 *   <li>{@link #NOTHING} — Ничего не изменено</li>
 *   <li>{@link #FAILED} — Операция провалилась</li>
 * </ul>
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.types;

public enum ServiceProcessType {
	/** Ресурс успешно найден */
	FOUNDED,
	/** Ресурс успешно создан */
	CREATED,
	/** Ресурс успешно обновлён */
	UPDATED,
	/** Ресурс успешно посчитан */
	COUNTED,
	/** Ресурс успешно восстановлен из удаленных */
	RESTORED,
	/** Ресурс успешно удалён */
	DELETED,

	/** Ничего не изменено */
	NOTHING,
}
