
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
 * @author Dmytro Shakh
 */

package com.spendi.core.router;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.core.base.http.RequestAttr;
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.middleware.BodyValidationMiddleware;
import com.spendi.core.middleware.JsonBodyParserMiddleware;
import com.spendi.core.response.ApiSuccessResponse;
import com.spendi.core.utils.AppVersion;

public class PingRouter extends ApiRouter {

	public PingRouter(String apiPrefix) {
		super(PingRouter.class.getSimpleName(), "/ping", apiPrefix);
	}

	@Override
	public void configure(HttpServerAdapter http) {
		// GET /ping
		this.get("/", ctx -> {
			ctx.res().status(200).sendJson(
					Map.of("status", "ok"));
		});

		// GET /ping/version
		this.get("/version", ctx -> {
			ctx.res().success(ApiSuccessResponse.ok(
					ctx.getRequestId(), "ok",
					Map.of(
							"status", "ok",
							"version", AppVersion.get())));
		});
	}
}
