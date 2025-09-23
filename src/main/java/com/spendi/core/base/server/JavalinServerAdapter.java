
/**
 * @file JavalinServerAdapter.java
 * @module core/base/server
 * 
 * @see BaseClass
 * @see BaseRouter
 * @see Route
 * @see HttpMethod
 * @see EClassType
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.server;

/**
 * ! lib imports
 */
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JavalinJackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * ! java imports
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseClass;
import com.spendi.core.base.BaseRouter;
import com.spendi.core.base.http.Middleware;
import com.spendi.core.base.http.MiddlewareChain;
import com.spendi.core.base.http.RouteHandler;
import com.spendi.core.base.router.Route;
import com.spendi.core.base.server.javalin.JavalinHttpContext;
import com.spendi.core.exceptions.DomainException;
import com.spendi.core.exceptions.ErrorCode;
import com.spendi.core.http.HttpMethod;
import com.spendi.core.response.ApiErrorResponse;
import com.spendi.core.types.EClassType;

public class JavalinServerAdapter extends BaseClass implements HttpServerAdapter {
	private final Javalin app;
	private final List<Middleware> globalMiddleware = new ArrayList<>();

	private ExceptionMapper exceptionMapper;

	public JavalinServerAdapter() {
		super(EClassType.SYSTEM, JavalinServerAdapter.class.getSimpleName());

		ObjectMapper mapper = new ObjectMapper().findAndRegisterModules() // находим и регистрируем все модули
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		this.app = Javalin.create(cfg -> {
			cfg.jsonMapper(new JavalinJackson(mapper, false));
		});

		app.exception(Exception.class, (e, jctx) -> {
			var httpCtx = new JavalinHttpContext(jctx);
			httpCtx.setSuccess(false);

			DomainException domain = null;

			if (e instanceof DomainException de) {
				// Уже готовый доменный эксепшен
				domain = de;
			} else if (exceptionMapper != null) {
				domain = exceptionMapper.toDomainException(e);
			}

			if (domain == null) {
				// Совсем неожиданный случай
				domain = new DomainException("Internal server error", ErrorCode.INTERNAL_ERROR,
						Map.of("exception", e.getClass().getName()), Map.of()) {};
				logError(domain, "Unhandled exception", httpCtx.getRequestId());
			} else {
				// Ожидаемое бизнес-исключение
				this.warn("Domain exception handled", httpCtx.getRequestId(),
						detailsOf("errorCode", domain.getErrorCodeName(), "details", domain.getDetails(), "fieldErrors",
								domain.getFieldErrors(), "message", domain.getMessage()),
						true);
			}

			ApiErrorResponse body = domain.toErrorResponse(httpCtx.getRequestId());
			httpCtx.res().error(body);
		});

	}

	/** Удобный метод: пробросить себя в роутер и смонтировать его содержимое. */
	public void registerRouter(BaseRouter router) {
		router.configure(this);
		this.mount(router.middlewares(), router.routes(), router.basePath(), router.getClassName());
	}

	public void setExceptionMapper(ExceptionMapper mapper) {
		this.exceptionMapper = mapper;
		this.info("ExceptionMapper installed", "no-id", detailsOf("class", mapper.getClass().getSimpleName()), true);
	}

	// ============================
	// HttpServerAdapter
	// ============================
	@Override
	public void useBefore(Middleware middleware) {
		if (middleware == null) {
			return;
		}
		this.globalMiddleware.add(middleware);

		this.info("Register middleware", "no-id",
				detailsOf("class", middleware.getClass().getSimpleName(), "type", "before", "scope", "global"), true);

		this.app.before(ctx -> {
			var httpCtx = new JavalinHttpContext(ctx);

			// одна глобальная миддла = единичная цепь
			var chain = new SingleMiddlewareChain(() -> {
				/* next=ничего */ });
			try {
				middleware.handle(httpCtx, chain);
			} catch (Exception e) {
				// Логируем и пробрасываем дальше — обработается в exceptionHandler'е
				logError(e, "Global middleware error", httpCtx.getRequestId());
				throw e;
			}
		});
	}

	// до: void use(Middleware middleware)
	public void useAfter(Middleware middleware) {
		if (middleware == null) {
			return;
		}
		this.globalMiddleware.add(middleware);

		this.info("Register middleware", "no-id",
				detailsOf("class", middleware.getClass().getSimpleName(), "type", "after", "scope", "global"), true);

		this.app.after(ctx -> {
			var httpCtx = new JavalinHttpContext(ctx);

			var chain = new SingleMiddlewareChain(() -> {
				/* no-op */});
			try {
				middleware.handle(httpCtx, chain);
			} catch (Exception e) {
				logError(e, "After middleware error", httpCtx.getRequestId());
				throw e;
			}
		});
	}

	@Override
	public void mount(List<Middleware> routerMiddlewares, List<Route> routes, String basePath, String name) {
		if (routerMiddlewares != null) {
			for (Middleware mw : routerMiddlewares) {
				if (mw == null)
					continue;

				this.info("Register middleware", "no-id", detailsOf("class", mw.getClass().getSimpleName(), "type",
						"before", "scope", "router", "basePath", basePath), true);

				if (basePath == null || basePath.isBlank() || "/".equals(basePath)) {
					// Fallback: глобальная регистрация (как было раньше)
					this.app.before(ctx -> invokeRouterMiddleware(mw, ctx));
				} else {
					String base = basePath;
					// на базовый путь
					this.app.before(base, ctx -> invokeRouterMiddleware(mw, ctx));
					// и на все вложенные пути
					String wildcard = base.endsWith("/") ? base + "*" : base + "/*";
					this.app.before(wildcard, ctx -> invokeRouterMiddleware(mw, ctx));
				}
			}
		}

		// Routes
		if (routes != null) {
			for (Route r : routes) {
				registerRoute(r, name);
			}
			this.info("Mounted routes", "no-id", detailsOf("count", routes.size()), true);
		} else {
			this.warn("Mounted routes", "no-id", detailsOf("count", 0), true);
		}
	}

	@Override
	public void start(int port) {

		this.info("Starting server", "no-id", detailsOf("port", port, "finished", false), true);

		this.app.start(port);
		this.info("Starting server", "no-id", detailsOf("port", port, "finished", true), true);

		this.debug("=== === ===  Server started === === === ");
	}

	@Override
	public void stop() {
		app.stop();
	}

	// ============================
	// Внутренние хелперы
	// ============================
	private void registerRoute(Route r, String className) {

		HttpMethod m = r.method();
		String path = r.path();
		RouteHandler handler = r.handler();
		List<Middleware> locals = r.middlewares();

		this.info("Register route " + className, "no-id", detailsOf("path", path, "method", m.name()), true);

		switch (m) {
		case GET -> app.get(path, ctx -> handleWithChain(ctx, handler, locals));
		case POST -> app.post(path, ctx -> handleWithChain(ctx, handler, locals));
		case PUT -> app.put(path, ctx -> handleWithChain(ctx, handler, locals));
		case PATCH -> app.patch(path, ctx -> handleWithChain(ctx, handler, locals));
		case DELETE -> app.delete(path, ctx -> handleWithChain(ctx, handler, locals));
		default -> throw new IllegalArgumentException("Unexpected value: " + m);
		}
	}

	private void handleWithChain(Context ctx, RouteHandler handler, List<Middleware> locals) throws Exception {
		var httpCtx = new JavalinHttpContext(ctx);

		// логируем входящий запрос
		this.info("Incoming request", httpCtx.getRequestId(), detailsOf("path", httpCtx.req().path(), "method",
				httpCtx.req().method(), "query", httpCtx.req().queryParams()), true);

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

	private void invokeRouterMiddleware(Middleware mw, Context ctx) throws Exception {
		var httpCtx = new JavalinHttpContext(ctx);
		var chain = new SingleMiddlewareChain(() -> {
			/* next=ничего */
		});
		try {
			mw.handle(httpCtx, chain);
		} catch (Exception e) {
			logError(e, "Router middleware error", httpCtx.getRequestId());
			throw e;
		}
	}

	private void logError(Throwable e, String message, String requestId) {
		this.error(message, requestId,
				Map.of("exception", e.getClass().getName(), "message", String.valueOf(e.getMessage())), true);
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
