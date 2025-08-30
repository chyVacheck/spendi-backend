
/**
 * @file JavalinHttpResponse.java
 * @module core/base/server/javalin
 * 
 * @see HttpResponse
 * @see HttpMethod
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.server.javalin;

/**
* ! lib imports
*/
import io.javalin.http.Context;

/**
 * ! my imports
 */
import com.spendi.core.base.http.HttpResponse;

public final class JavalinHttpResponse implements HttpResponse {
	private final Context ctx;

	public JavalinHttpResponse(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public int getStatus() {
		return ctx.status().getCode(); // возвращает текущий статус в числовом виде
	}

	@Override
	public HttpResponse status(int statusCode) {
		ctx.status(statusCode);
		return this;
	}

	@Override
	public HttpResponse header(String name, String value) {
		ctx.header(name, value);
		return this;
	}

	@Override
	public void sendText(String text) {
		ctx.result(text);
	}

	@Override
	public void sendBytes(byte[] bytes) {
		ctx.result(bytes);
	}

	@Override
	public void sendJson(Object body) {
		ctx.json(body);
	}

	public Context raw() {
		return ctx;
	}
}
