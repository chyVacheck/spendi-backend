
/**
 * @file RequestLifecycleMiddleware.java
 * @module core/middleware
 * 
 * @see BaseMiddleware
 * 
 * @description
 * Финальная middleware, завершающая жизненный цикл запроса:
 * - рассчитывает время обработки (нс + мс),
 * - выставляет заголовки (X-Request-Id, X-Response-Time, X-Response-Time-Nanos, X-Request-Heavy),
 * - логирует итог запроса.
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.middleware;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMiddleware;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.MiddlewareChain;

public class RequestLifecycleMiddleware extends BaseMiddleware {
	// порог «тяжёлого» запроса в мс
	private static final long HEAVY_REQUEST_THRESHOLD_MS = 800;

	public RequestLifecycleMiddleware() {
		super(RequestLifecycleMiddleware.class.getSimpleName());
	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		try {
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
