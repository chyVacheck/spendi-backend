
/**
 * @file RouteHandler.java
 * @module core/base/http
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.http;

/**
 * Функциональный обработчик маршрута: (ctx) -> void
 */
@FunctionalInterface
public interface RouteHandler {
	void handle(HttpContext ctx) throws Exception;
}
