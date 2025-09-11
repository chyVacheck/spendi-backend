/**
 * @file BaseController.java
 * @module core/base
 *
 * @description
 * Базовый абстрактный класс для всех контроллеров в приложении.
 * Предоставляет общую функциональность и структуру для обработки HTTP-запросов.
 * 
 * Наследуется от BaseClass, что обеспечивает:
 * - Единообразное логирование через встроенные методы (info, debug, error)
 * - Стандартизированную структуру классов с типизацией (CONTROLLER)
 * - Общие утилиты для работы с деталями запросов
 * 
 * Все контроллеры в системе должны наследоваться от этого класса
 * для обеспечения консистентности архитектуры.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;

/**
 * Базовый абстрактный контроллер для всех HTTP-обработчиков в приложении.
 * 
 * <p>
 * Этот класс служит основой для всех контроллеров и обеспечивает:
 * <ul>
 * <li>Единообразную структуру наследования</li>
 * <li>Доступ к методам логирования из BaseClass</li>
 * <li>Правильную типизацию класса как CONTROLLER</li>
 * <li>Стандартизированную инициализацию</li>
 * </ul>
 * 
 * <p>
 * Пример использования:
 * 
 * <pre>{@code
 * public class UserController extends BaseController {
 * 	public UserController() {
 * 		super(UserController.class.getSimpleName());
 * 	}
 * 
 * 	public void getUsers(HttpContext ctx) {
 * 		this.info("Getting users", ctx.getRequestId());
 * 		// логика обработки
 * 	}
 * }
 * }</pre>
 * 
 * @see BaseClass
 * @see EClassType#CONTROLLER
 */
public abstract class BaseController extends BaseClass {

	/**
	 * Конструктор базового контроллера.
	 * 
	 * <p>
	 * Инициализирует контроллер с указанным именем класса и устанавливает
	 * тип класса как CONTROLLER для правильной категоризации в системе логирования
	 * и метрик.
	 * 
	 * @param className человекочитаемое имя класса контроллера (обычно получается
	 *                  через
	 *                  {@code ClassName.class.getSimpleName()})
	 * 
	 * @see BaseClass#BaseClass(EClassType, String)
	 */
	public BaseController(String className) {
		super(EClassType.CONTROLLER, className);
	}
}
