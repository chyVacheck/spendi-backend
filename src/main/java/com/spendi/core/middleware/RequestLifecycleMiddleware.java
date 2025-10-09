
/**
 * @file RequestLifecycleMiddleware.java
 * @module core/middleware
 * 
 * @see BaseMiddleware
 * 
 * @description Финальная middleware, завершающая жизненный цикл запроса: - рассчитывает время
 *              обработки (нс + мс), - выставляет заголовки (X-Request-Id, X-Response-Time,
 *              X-Response-Time-Nanos, X-Request-Heavy), - логирует итог запроса.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.middleware;

import java.util.List;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMiddleware;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.MiddlewareChain;

public class RequestLifecycleMiddleware extends BaseMiddleware {
	// порог «тяжёлого» запроса в мс
	private static final long HEAVY_REQUEST_THRESHOLD_MS = 800;

	private final List<String> allowedOrigins;

	public RequestLifecycleMiddleware(List<String> allowedOrigins) {
		super(RequestLifecycleMiddleware.class.getSimpleName());
		this.allowedOrigins = allowedOrigins;

	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		try {
			final String origin = ctx.req().header("Origin").orElse(null);

			if (origin != null && allowedOrigins.contains(origin)) {
				// Разрешаем конкретный origin (credentials требуют НЕ "*")
				ctx.res().header("Access-Control-Allow-Origin", origin);
				ctx.res().header("Vary", "Origin");
				ctx.res().header("Access-Control-Allow-Credentials", "true");
				ctx.res().header("Access-Control-Allow-Methods",
						"GET,POST,PUT,PATCH,DELETE,OPTIONS");
				ctx.res().header("Access-Control-Allow-Headers",
						"Content-Type, Authorization, X-Requested-With");
				ctx.res().header("Access-Control-Max-Age", "86400");

				// Экспонируем кастомные заголовки твоего API
				ctx.res().header("Access-Control-Expose-Headers",
						"X-Request-Id, X-Response-Time, X-Response-Time-Nanos, " +
								"X-Is-Successful, X-Entity-Changed, X-Request-Heavy");
			}

			// Preflight: коротко отвечаем 204 и выходим
			if ("OPTIONS".equalsIgnoreCase(ctx.req().method().name())) {
				ctx.res().status(204);
				return; // НЕ вызываем chain.next()
			}

			chain.next();
		} finally {
			// фрагмент внутри finally
			long durationNanos = System.nanoTime() - ctx.getStartNanos();
			long durationMs = durationNanos / 1_000_000;

			ctx.res().header("X-Request-Id", ctx.getRequestId());
			ctx.res().header("X-Response-Time-Nanos", String.valueOf(durationNanos));
			ctx.res().header("X-Response-Time", durationMs + "ms");
			ctx.res().header("X-Is-Succesfull", String.valueOf(ctx.isSuccess()));

			boolean isHeavyRequest = durationMs > HEAVY_REQUEST_THRESHOLD_MS;

			if (isHeavyRequest) {
				ctx.res().header("X-Request-Heavy", "true");
				this.warn("Heavy request", ctx.getRequestId(), detailsOf(
						"durationMs", durationMs,
						"durationNanos", durationNanos,
						"isHeavyRequest", isHeavyRequest,
						"isError", !ctx.isSuccess(),
						"statusCode", ctx.res().getStatus()), true);
			} else {
				// логируем входящий запрос
				this.info("Request processed", ctx.getRequestId(), detailsOf(
						"durationMs", durationMs,
						"durationNanos", durationNanos,
						"isError", !ctx.isSuccess(),
						"statusCode", ctx.res().getStatus()), true);
			}
		}
	}
}
