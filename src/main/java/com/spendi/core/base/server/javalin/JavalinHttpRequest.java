
/**
 * @file JavalinHttpRequest.java
 * @module core/base/server/javalin
 * 
 * @see BaseMiddleware
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
 * ! java imports
 */
import java.util.*;

/**
 * ! my imports
 */
import com.spendi.core.base.http.HttpRequest;
import com.spendi.core.http.HttpMethod;

public final class JavalinHttpRequest implements HttpRequest {
	private final Context ctx;

	public JavalinHttpRequest(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	public HttpMethod method() {
		return HttpMethod.valueOf(ctx.method().name());
	}

	@Override
	public String path() {
		return ctx.path();
	}

	@Override
	public Optional<String> pathParam(String name) {
		try {
			String v = ctx.pathParam(name);
			return Optional.ofNullable(v);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Map<String, String> pathParams() {
		return Collections.unmodifiableMap(ctx.pathParamMap());
	}

	@Override
	public Optional<String> queryParam(String name) {
		return Optional.ofNullable(ctx.queryParam(name));
	}

	@Override
	public Map<String, List<String>> queryParams() {
		Map<String, List<String>> m = new LinkedHashMap<>();
		ctx.queryParamMap().forEach((k, v) -> m.put(k, List.copyOf(v)));
		return Collections.unmodifiableMap(m);
	}

	@Override
	public Optional<String> header(String name) {
		return Optional.ofNullable(ctx.header(name));
	}

	@Override
	public Map<String, String> headers() {
		Map<String, String> m = new LinkedHashMap<>();
		ctx.headerMap().forEach((k, v) -> m.put(k, v));
		return Collections.unmodifiableMap(m);
	}

	@Override
	public Optional<String> bodyAsString() {
		try {
			return Optional.ofNullable(ctx.body());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<byte[]> bodyAsBytes() {
		try {
			return Optional.ofNullable(ctx.bodyAsBytes());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> remoteAddress() {
		return Optional.ofNullable(ctx.req().getRemoteAddr());
	}

	public Context raw() {
		return ctx;
	}
}
