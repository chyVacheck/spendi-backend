
/**
 * @file HttpServerAdapter.java
 * @module core/base/server
 *
 * @see Middleware
 * @see Route
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.server;

/**
 * ! java imports
 */
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.core.base.http.Middleware;
import com.spendi.core.base.router.Route;

/**
 * Абстракция над конкретным web-сервером/фреймворком.
 * Реализации: JavalinServerAdapter, UndertowServerAdapter и т.п.
 */
public interface HttpServerAdapter {

	/** Зарегистрировать глобальные middleware (до всех маршрутов). */
	void useBefore(Middleware middleware);

	/**
	 * Смонтировать список маршрутов (уже с абсолютными путями) + локальные
	 * роутер-мидлвары.
	 * Обычно вызывается для каждого роутера после его configure().
	 */
	void mount(List<Middleware> routerMiddlewares, List<Route> routes);

	/** Запуск/остановка сервера. */
	void start(int port);

	void stop();
}