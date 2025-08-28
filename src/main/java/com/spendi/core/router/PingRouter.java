/**
 * @file PingRouter.java
 * @module core/router
 *
 * @description
 * Простейший роутер для health-check. Используется для проверки,
 * что сервер работает (например, в Kubernetes livenessProbe).
 *
 * GET /ping -> { "status": "ok" }
 *
 * @author
 * Dmytro Shakh
 */

package com.spendi.core.router;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRouter;
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.utils.AppVersion;

public class PingRouter extends BaseRouter {

	public PingRouter() {
		super(PingRouter.class.getSimpleName(), "/ping");
	}

	@Override
	public void configure(HttpServerAdapter http) {
		// GET /ping
		this.get("/", ctx -> {
			ctx.res().status(200).sendJson(
					Map.of("status", "ok"));
		});

		// GET /ping
		this.get("/version", ctx -> {
			ctx.res().status(200).sendJson(
					Map.of(
							"status", "ok",
							"version", AppVersion.get()));
		});
	}
}
