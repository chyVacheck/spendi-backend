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
import com.fasterxml.jackson.databind.JsonNode;

/**
 * ! java imports
 */
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.nio.file.Path;

/**
 * ! my imports
 */
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.HttpRequest;
import com.spendi.core.base.http.HttpResponse;
import com.spendi.core.base.http.RequestAttr;
import com.spendi.core.files.UploadedFile;
import com.spendi.modules.session.SessionEntity;

public final class JavalinHttpContext implements HttpContext {

	// Ключ «кармана» атрибутов внутри Javalin Context
	private static final String ATTR_MAP_KEY = "__http_attrs";

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
		attrs.putIfAbsent(RequestAttr.REQUEST_ID, shortRequestId());
		attrs.putIfAbsent(RequestAttr.START_NANOS, System.nanoTime());
		attrs.putIfAbsent(RequestAttr.SUCCESS, Boolean.TRUE);

		// Обёртки над запросом/ответом
		this.request = new JavalinHttpRequest(jctx);
		this.response = new JavalinHttpResponse(jctx);
	}

	public Context raw() {
		return jctx;
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
		return getAttr(RequestAttr.REQUEST_ID, String.class);
	}

	@Override
	public long getStartNanos() {
		Long v = getAttr(RequestAttr.START_NANOS, Long.class);
		return (v != null) ? v : 0L;
	}

	@Override
	public boolean isSuccess() {
		Boolean v = getAttr(RequestAttr.SUCCESS, Boolean.class);
		return v != null && v;
	}

	@Override
	public void setSuccess(boolean success) {
		setAttr(RequestAttr.SUCCESS, success);
	}

	@Override
	public byte[] getRawBody() {
		return getAttr(RequestAttr.RAW_BODY, byte[].class);
	}

	@Override
	public JsonNode getRawJson() {
		return getAttr(RequestAttr.RAW_JSON, JsonNode.class);
	}

	@Override
	public <T> T getValidBody(Class<T> type) {
		return getAttr(RequestAttr.VALID_BODY, type);
	}

	@Override
	public <T> T getValidParams(Class<T> type) {
		return getAttr(RequestAttr.VALID_PARAMS, type);
	}

	@Override
	public <T> T getValidQuery(Class<T> type) {
		return getAttr(RequestAttr.VALID_QUERY, type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<UploadedFile> getFiles() {
		return getAttr(RequestAttr.FILES, List.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Path> getTempFiles() {
		return getAttr(RequestAttr.TEMP_FILES, List.class);
	}

	@Override
	public SessionEntity getAuthSession() {
		return getAttr(RequestAttr.AUTH_SESSION, SessionEntity.class);
	}

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
