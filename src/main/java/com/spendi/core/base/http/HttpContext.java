
/**
 * @file HttpContext.java
 * @module core/base/http
 *
 * @author Dmytro Shakh
 */

package com.spendi.core.base.http;

/**
 * ! java imports
 */
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Общий контекст обработки запроса: Request + Response + произвольные атрибуты.
 */
public final class HttpContext {
	public static final String ATTR_REQUEST_ID = "requestId";
	public static final String ATTR_START_NANOS = "startNanos";
	public static final String ATTR_IS_SUCCESS = "isSuccess";

	private final HttpRequest request;
	private final HttpResponse response;

	// фиксируем сразу при создании контекста
	private final String requestId;
	private final long startNanos;

	private final Map<String, Object> attributes = new ConcurrentHashMap<>();

	public HttpContext(HttpRequest request, HttpResponse response) {
		this.request = request;
		this.response = response;

		this.requestId = UUID.randomUUID().toString().substring(0, 8);
		this.startNanos = System.nanoTime();

		// дублируем в атрибуты — вдруг где-то удобно читать как generic-значения
		attributes.put(ATTR_REQUEST_ID, requestId);
		attributes.put(ATTR_START_NANOS, startNanos);
	}

	public HttpRequest req() {
		return request;
	}

	public HttpResponse res() {
		return response;
	}

	/** стабильный requestId на весь жизненный цикл запроса */
	public String getRequestId() {
		return requestId;
	}

	/** момент старта запроса в нс (System.nanoTime()) */
	public long getStartNanos() {
		return startNanos;
	}

	/** бизнес-успешность (опционально, можно дергать из хендлеров/миддвар) */
	public void setSuccess(boolean value) {
		attributes.put(ATTR_IS_SUCCESS, value);
	}

	public boolean isSuccess() {
		Object v = attributes.get(ATTR_IS_SUCCESS);
		return v instanceof Boolean b && b;
	}

	/**
	 * Атрибуты middleware/handler'ов (например, requestId).
	 */
	public void setAttr(String key, Object value) {
		attributes.put(key, value);
	}

	public Object getAttr(String key) {
		return attributes.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttr(String key, Class<T> type) {
		Object v = attributes.get(key);
		return (v != null && type.isInstance(v)) ? (T) v : null;
	}
}