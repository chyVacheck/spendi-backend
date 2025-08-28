/**
 * @file BaseRouter.java
 * @module core/base
 *
 * @see EClassType
 * @see HttpMethod
 * @see Route
 * @see Middleware
 * @see RouteHandler
 * 
 * @author Dmytro Shakh
 */

package com.spendi.core.base;

/**
 * ! java imports
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ! my imports
 */
import com.spendi.core.types.EClassType;
import com.spendi.core.http.HttpMethod;
import com.spendi.core.base.router.Route;
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.base.http.Middleware;
import com.spendi.core.base.http.RouteHandler;

public abstract class BaseRouter extends BaseClass {
	private final String basePath;
	private final List<Middleware> middlewares = new ArrayList<>();
	private final List<Route> routes = new ArrayList<>();

	protected BaseRouter(String className, String basePath) {
		super(EClassType.ROUTER, className);
		this.basePath = normalizeBase(basePath);
	}

	/** Инициализация маршрутов/мидлваров — реализовать в наследнике. */
	public abstract void configure(HttpServerAdapter http);

	public String basePath() {
		return basePath;
	}

	public List<Middleware> middlewares() {
		return Collections.unmodifiableList(middlewares);
	}

	public List<Route> routes() {
		return Collections.unmodifiableList(routes);
	}

	/**
	 * Добавить middleware на весь роутер.
	 */
	protected void use(Middleware mw) {
		if (mw != null)
			middlewares.add(mw);
	}

	/**
	 * Зарегистрировать маршрут с локальными middleware.
	 */
	protected void route(HttpMethod method, String subPath, RouteHandler handler, Middleware... local) {
		String full = join(basePath, subPath);
		routes.add(Route.of(method, full, handler, local));
	}

	// Хелперы: get/post/put/delete/patch
	protected void get(String subPath, RouteHandler h, Middleware... mw) {
		route(HttpMethod.GET, subPath, h, mw);
	}

	protected void post(String subPath, RouteHandler h, Middleware... mw) {
		route(HttpMethod.POST, subPath, h, mw);
	}

	protected void put(String subPath, RouteHandler h, Middleware... mw) {
		route(HttpMethod.PUT, subPath, h, mw);
	}

	protected void patch(String subPath, RouteHandler h, Middleware... mw) {
		route(HttpMethod.PATCH, subPath, h, mw);
	}

	protected void delete(String subPath, RouteHandler h, Middleware... mw) {
		route(HttpMethod.DELETE, subPath, h, mw);
	}

	private static String normalizeBase(String base) {
		if (base == null || base.isBlank())
			return "/";
		String b = base.startsWith("/") ? base : "/" + base;
		return b.endsWith("/") && b.length() > 1 ? b.substring(0, b.length() - 1) : b;
	}

	private static String join(String base, String sub) {
		if (sub == null || sub.isBlank() || "/".equals(sub))
			return base;
		String s = sub.startsWith("/") ? sub : "/" + sub;
		return (base.equals("/")) ? s : base + s;
	}
}
