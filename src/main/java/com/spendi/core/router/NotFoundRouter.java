
/**
 * @file NotFoundRouter.java
 * @module core/router
 *
 * @description
 * Простейший роутер для . Используется для уведомления клиента о не существовании роутера.
 *
 * GET, POST .. "/*" -> RouterNotFoundException. 
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.router;

/**
 * ! my imports
 */
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.server.HttpServerAdapter;
import com.spendi.core.exceptions.RouterNotFoundException;

public class NotFoundRouter extends ApiRouter {

	public NotFoundRouter(String apiPrefix) {
		super(NotFoundRouter.class.getSimpleName(), "/", apiPrefix);
	}

	@Override
	public void configure(HttpServerAdapter http) {
		this.get("/*", ctx -> respond(ctx));
		this.post("/*", ctx -> respond(ctx));
		this.put("/*", ctx -> respond(ctx));
		this.patch("/*", ctx -> respond(ctx));
		this.delete("/*", ctx -> respond(ctx));
	}

	private void respond(HttpContext ctx) throws RouterNotFoundException {
		throw new RouterNotFoundException(ctx.req().path());
	}
}
