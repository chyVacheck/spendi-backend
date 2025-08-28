
/**
 * @file Route.java
 * @module core/base/router
 *
 * @see Middleware
 * @see RouteHandler
 * @see HttpMethod
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.router;

/**
 * ! java imports
 */
import java.util.Arrays;
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.core.base.http.Middleware;
import com.spendi.core.base.http.RouteHandler;
import com.spendi.core.http.HttpMethod;

/** Value Object маршрута: метод + путь + хендлер + локальные middleware. */
public record Route(
		HttpMethod method,
		String path,
		RouteHandler handler,
		List<Middleware> middlewares) {
	public static Route of(HttpMethod method, String path, RouteHandler handler, Middleware... mws) {
		return new Route(method, path, handler, (mws == null) ? List.of() : Arrays.asList(mws));
	}
}
