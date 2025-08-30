
/**
 * @file JavalinServerAdapter.java
 * @module core/base/server
 * 
 * @see BaseClass
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
import com.spendi.core.logger.types.LogOptions;
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

		ObjectMapper mapper = new ObjectMapper()
				.findAndRegisterModules()
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		this.app = Javalin.create(cfg -> {
			cfg.jsonMapper(new JavalinJackson(mapper));
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
				domain = new DomainException(
						"Internal server error",
						ErrorCode.INTERNAL_ERROR,
						Map.of("exception", e.getClass().getName()),
						Map.of()) {
				};
				logError(domain, "Unhandled exception", httpCtx.getRequestId());
			} else {
				// Ожидаемое бизнес-исключение
				this.warn("Domain exception handled", httpCtx.getRequestId(), detailsOf(
						"errorCode", domain.getErrorCodeName(),
						"message", domain.getMessage()));
			}

			ApiErrorResponse body = domain.toErrorResponse(httpCtx.getRequestId());
			httpCtx.res().error(body);
		});

	}

	/** Удобный метод: пробросить себя в роутер и смонтировать его содержимое. */
	public void registerRouter(BaseRouter router) {
		router.configure(this);
		this.mount(router.middlewares(), router.routes());
	}

	public void setExceptionMapper(ExceptionMapper mapper) {
		this.exceptionMapper = mapper;
		this.info("ExceptionMapper installed", detailsOf(
				"class", mapper.getClass().getSimpleName()));
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

		this.info("Register global middleware", detailsOf(
				"class", middleware.getClass().getSimpleName()));

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

		this.info("Register AFTER middleware", detailsOf(
				"class", middleware.getClass().getSimpleName()));

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
	public void mount(List<Middleware> routerMiddlewares, List<Route> routes) {
		// Router-level middlewares: onBefore для каждого пути (через AnyPath)
		if (routerMiddlewares != null) {
			for (Middleware mw : routerMiddlewares) {
				if (mw == null)
					continue;

				this.info("Register router middleware", detailsOf(
						"class", mw.getClass().getSimpleName()));

				this.app.before(ctx -> {
					var httpCtx = new JavalinHttpContext(ctx);

					var chain = new SingleMiddlewareChain(() -> {
						/* next=ничего */});
					try {
						mw.handle(httpCtx, chain);
					} catch (Exception e) {
						logError(e, "Router middleware error", httpCtx.getRequestId());
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
			this.info("Mounted routes", detailsOf("count", routes.size()));
		}
	}

	@Override
	public void start(int port) {
		this.info("Starting HTTP server", detailsOf("port", port));
		this.app.start(port);
		this.info("HTTP server started", detailsOf("port", port));
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

		this.info("Register route", detailsOf(
				"method", m.name(),
				"path", path));

		switch (m) {
			case GET -> app.get(path, ctx -> handleWithChain(ctx, handler, locals));
			case POST -> app.post(path, ctx -> handleWithChain(ctx, handler, locals));
			case PUT -> app.put(path, ctx -> handleWithChain(ctx, handler, locals));
			case PATCH -> app.patch(path, ctx -> handleWithChain(ctx, handler, locals));
			case DELETE -> app.delete(path, ctx -> handleWithChain(ctx, handler, locals));
			default -> throw new IllegalArgumentException("Unexpected value: " + m);
		}
	}

	private void handleWithChain(Context ctx, RouteHandler handler, List<Middleware> locals)
			throws Exception {
		var httpCtx = new JavalinHttpContext(ctx);

		// логируем входящий запрос
		this.info("Incoming request", httpCtx.getRequestId(), detailsOf(
				"method", httpCtx.req().method(),
				"path", httpCtx.req().path(),
				"query", httpCtx.req().queryParams()), true);

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

	private void logError(Throwable e, String message, String requestId) {
		this.error(message,
				requestId,
				Map.of("exception", e.getClass().getName(), "message", String.valueOf(e.getMessage())),
				new LogOptions(true));
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
