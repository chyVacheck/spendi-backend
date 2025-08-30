/**
 * @file JavalinHttpContext.java
 * @module core/base/server/javalin
 *
 * Реализация HttpContext поверх io.javalin.http.Context.
 * Все per-request атрибуты живут в самом Javalin Context через attribute-карман.
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ! my imports
 */
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.HttpRequest;
import com.spendi.core.base.http.HttpResponse;

public final class JavalinHttpContext implements HttpContext {

	// Ключ «кармана» атрибутов внутри Javalin Context
	private static final String ATTR_MAP_KEY = "__http_attrs";

	// Ключи «системных» атрибутов
	private static final String KEY_REQUEST_ID = "requestId";
	private static final String KEY_START_NANOS = "startNanos";
	private static final String KEY_SUCCESS = "success";

	private final Context jctx;
	private final JavalinHttpRequest request;
	private final JavalinHttpResponse response;

	public JavalinHttpContext(Context jctx) {
		this.jctx = jctx;

		// Ленивая/первичная инициализация кармана атрибутов
		Map<String, Object> attrs = jctx.attribute(ATTR_MAP_KEY);
		if (attrs == null) {
			attrs = new HashMap<>();
			jctx.attribute(ATTR_MAP_KEY, attrs);
		}

		// Гарантируем базовые значения один раз на запрос
		attrs.putIfAbsent(KEY_REQUEST_ID, shortRequestId());
		attrs.putIfAbsent(KEY_START_NANOS, System.nanoTime());
		attrs.putIfAbsent(KEY_SUCCESS, Boolean.TRUE);

		// Обёртки над запросом/ответом
		this.request = new JavalinHttpRequest(jctx);
		this.response = new JavalinHttpResponse(jctx);
	}

	/*
	 * =========================
	 * Реализация HttpContext
	 * =========================
	 */

	public HttpRequest req() {
		return request;
	}

	public HttpResponse res() {
		return response;
	}

	public void setAttr(String key, Object value) {
		attrs().put(key, value);
	}

	public Object getAttr(String key) {
		return attrs().get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttr(String key, Class<T> type) {
		Object v = attrs().get(key);
		return (v != null && type.isInstance(v)) ? (T) v : null;
	}

	/*
	 * =========================
	 * Удобные системные геттеры
	 * =========================
	 */

	public String getRequestId() {
		return getAttr(KEY_REQUEST_ID, String.class);
	}

	public long getStartNanos() {
		Long v = getAttr(KEY_START_NANOS, Long.class);
		return (v != null) ? v : 0L;
	}

	public boolean isSuccess() {
		Boolean v = getAttr(KEY_SUCCESS, Boolean.class);
		return v != null && v;
	}

	public void setSuccess(boolean success) {
		setAttr(KEY_SUCCESS, success);
	}

	/*
	 * =========================
	 * Внутренние помощники
	 * =========================
	 */

	/** Достаём/создаём общий карман атрибутов. */
	private Map<String, Object> attrs() {
		Map<String, Object> map = jctx.attribute(ATTR_MAP_KEY);
		if (map == null) {
			map = new HashMap<>();
			jctx.attribute(ATTR_MAP_KEY, map);
		}
		return map;
	}

	private static String shortRequestId() {
		// короткий UUID (8 символов)
		return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	}
}