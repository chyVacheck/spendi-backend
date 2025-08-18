/**
 * @module com.spendi.core.types.EClassType
 * @description
 * Перечисление всех возможных типов классов в системе.
 *
 * <p>Назначение данного перечисления:</p>
 * <ul>
 *   <li>Определяет роль и назначение каждого класса в архитектуре</li>
 *   <li>Используется для логирования, мониторинга и отладки</li>
 *   <li>Помогает явно разграничить ответственность между компонентами системы</li>
 * </ul>
 *
 * <p>Примеры использования:</p>
 * <pre>{@code
 * public class UserService extends CoreClass {
 *     public UserService() {
 *         super(EClassType.SERVICE, "UserService");
 *     }
 * }
 * }</pre>
 *
 * <p>Типы классов:</p>
 * <ul>
 *   <li>CONTROLLER — контроллеры REST API</li>
 *   <li>SERVICE — сервисы бизнес-логики</li>
 *   <li>REPOSITORY — слой доступа к данным (работа с базой данных)</li>
 *   <li>MAPPER — преобразование DTO ↔ Entity</li>
 *   <li>ROUTER — регистрация контроллеров и middleware</li>
 *   <li>MIDDLEWARE — промежуточная обработка запросов</li>
 *   <li>GUARD — проверки разрешений и безопасности</li>
 *   <li>DATABASE — классы работы с базами данных</li>
 *   <li>SYSTEM — внутренние системные классы</li>
 *   <li>UTIL — утилитарные вспомогательные классы</li>
 *   <li>UNKNOWN — неопределённый или нераспознанный класс</li>
 * </ul>
 *
 * @author Dmytro Shakh
 */
package com.spendi.core.types;

public enum EClassType {
	CONTROLLER,
	SERVICE,
	REPOSITORY,
	MAPPER,
	ROUTER,
	MIDDLEWARE,
	GUARD,
	DATABASE,
	SYSTEM,
	UTIL,
	UNKNOWN
}
