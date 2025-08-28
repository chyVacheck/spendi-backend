
/**
 * @file JavalinServerAdapter.java
 * @module core/base/server
 * 
 * @see BaseRouter
 * @see Route
 * @see HttpMethod
 * @see Logger
 * @see LogData
 * @see LogOptions
 * @see EClassType
 * @see JavalinHttpRequest
 * @see JavalinHttpResponse
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.server;

/**
 * ! lib imports
 */
import io.javalin.Javalin;

/**
 * ! java imports
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRouter;
import com.spendi.core.base.http.*;
import com.spendi.core.base.router.Route;
import com.spendi.core.http.HttpMethod;
import com.spendi.core.logger.Logger;
import com.spendi.core.logger.model.LogData;
import com.spendi.core.logger.types.LogOptions;
import com.spendi.core.types.EClassType;
import com.spendi.core.base.server.javalin.JavalinHttpRequest;
import com.spendi.core.base.server.javalin.JavalinHttpResponse;

public class JavalinServerAdapter implements HttpServerAdapter {
	private final Javalin app;
	private final List<Middleware> globalMiddleware = new ArrayList<>();

	public JavalinServerAdapter(Javalin preconfigured) {
		this.app = Objects.requireNonNull(preconfigured, "Javalin instance must not be null");
	}

	public JavalinServerAdapter() {
		this(Javalin.create());
	}

	/** Удобный метод: пробросить себя в роутер и смонтировать его содержимое. */
	public void registerRouter(BaseRouter router) {
		router.configure(this);
		this.mount(router.middlewares(), router.routes(), null);
	}

	// ============================
	// HttpServerAdapter
	// ============================
	@Override
	public void use(Middleware middleware) {
		if (middleware == null)
			return;
		globalMiddleware.add(middleware);
		app.before(ctx -> {
			var req = new JavalinHttpRequest(ctx);
			var res = new JavalinHttpResponse(ctx);
			var httpCtx = new HttpContext(req, res);

			// одна глобальная миддла = единичная цепь
			var chain = new SingleMiddlewareChain(() -> {
				/* next=ничего */ });
			try {
				middleware.handle(httpCtx, chain);
			} catch (Exception e) {
				// Логируем и пробрасываем дальше — обработается в exceptionHandler'е
				logError(e, "Global middleware error");
				throw e;
			}
		});
	}

	@Override
	public void mount(List<Middleware> routerMiddlewares, List<Route> routes, ExceptionMapper exceptionMapper) {
		// Router-level middlewares: onBefore для каждого пути (через AnyPath)
		if (routerMiddlewares != null) {
			for (Middleware mw : routerMiddlewares) {
				if (mw == null)
					continue;
				app.before(ctx -> {
					var req = new JavalinHttpRequest(ctx);
					var res = new JavalinHttpResponse(ctx);
					var httpCtx = new HttpContext(req, res);

					var chain = new SingleMiddlewareChain(() -> {
						/* next=ничего */});
					try {
						mw.handle(httpCtx, chain);
					} catch (Exception e) {
						logError(e, "Router middleware error");
						throw e;
					}
				});
			}
		}

		// Routes
		if (routes != null) {
			for (Route r : routes) {
				registerRoute(r);
			}
		}

		// Exceptions
		app.exception(Exception.class, (e, ctx) -> {
			Exception toLog = e;
			if (exceptionMapper != null) {
				Exception mapped = exceptionMapper.toDomainException(e);
				if (mapped != null) {
					toLog = mapped;
				}
			}
			// Фоллбэк: вернём 500 и текст
			ctx.status(500).result("Internal Server Error");
			logError(toLog, "Unhandled exception");
		});

	}

	// Перегрузка, если пока нет ExceptionMapper
	public void mount(List<Middleware> routerMiddlewares, List<Route> routes) {
		mount(routerMiddlewares, routes, null);
	}

	@Override
	public void start(int port) {
		app.start(port);
	}

	@Override
	public void stop() {
		app.stop();
	}

	// ============================
	// Внутренние хелперы
	// ============================
	private void registerRoute(Route r) {
		HttpMethod m = r.method();
		String path = r.path();
		RouteHandler handler = r.handler();
		List<Middleware> locals = r.middlewares();

		switch (m) {
			case GET -> app.get(path, ctx -> handleWithChain(ctx, handler, locals));
			case POST -> app.post(path, ctx -> handleWithChain(ctx, handler, locals));
			case PUT -> app.put(path, ctx -> handleWithChain(ctx, handler, locals));
			case PATCH -> app.patch(path, ctx -> handleWithChain(ctx, handler, locals));
			case DELETE -> app.delete(path, ctx -> handleWithChain(ctx, handler, locals));
			default -> throw new IllegalArgumentException("Unexpected value: " + m);
		}
	}

	private void handleWithChain(io.javalin.http.Context ctx, RouteHandler handler, List<Middleware> locals)
			throws Exception {
		var req = new JavalinHttpRequest(ctx);
		var res = new JavalinHttpResponse(ctx);
		var httpCtx = new HttpContext(req, res);

		// Выполняем локальные миддлы “по цепочке”, затем — handler
		if (locals == null || locals.isEmpty()) {
			handler.handle(httpCtx);
			return;
		}

		// Собираем цепочку: mw1 -> mw2 -> ... -> handler
		MiddlewareChain terminal = () -> handler.handle(httpCtx);
		MiddlewareChain chain = terminal;
		// Идём с конца, чтобы next шёл вперёд
		for (int i = locals.size() - 1; i >= 0; i--) {
			Middleware current = locals.get(i);
			MiddlewareChain nextChain = chain;
			chain = () -> current.handle(httpCtx, nextChain);
		}
		chain.next();
	}

	private void logError(Throwable e, String message) {
		Logger.error(new LogData(
				message,
				null,
				java.util.Map.of("exception", e.getClass().getName(), "message", String.valueOf(e.getMessage())),
				new LogOptions(true),
				"JavalinServerAdapter",
				EClassType.SYSTEM));
	}

	/** Простейшая реализация цепочки для одного middleware. */
	private static final class SingleMiddlewareChain implements MiddlewareChain {
		private final Runnable next;

		SingleMiddlewareChain(Runnable next) {
			this.next = next;
		}

		@Override
		public void next() {
			next.run();
		}
	}
}
