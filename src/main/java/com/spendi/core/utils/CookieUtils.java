/**
 * @file CookieUtils.java
 * @module core/utils
 */

package com.spendi.core.utils;

import com.spendi.config.AuthConfig;
import com.spendi.core.base.http.HttpRequest;

/**
 * Утилиты для работы с Cookie и заголовками.
 */
public final class CookieUtils {

	private CookieUtils() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/** Прочитать значение cookie по имени. */
	public static String readCookie(HttpRequest req, String name) {
		String cookie = req.header("Cookie").orElse(null);
		if (cookie == null)
			return null;
		String[] parts = cookie.split(";\\s*");
		for (String p : parts) {
			int eq = p.indexOf('=');
			if (eq > 0) {
				String k = p.substring(0, eq).trim();
				String v = p.substring(eq + 1);
				if (k.equals(name))
					return v;
			}
		}
		return null;
	}

	/**
	 * Построить строку Set-Cookie с учётом настроек.
	 */
	public static String buildCookie(String name, String value, int maxAgeSec, AuthConfig cfg) {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("=").append(value == null ? "" : value)
				.append("; Path=").append(cfg.getCookiePath());
		if (cfg.getCookieDomain() != null && !cfg.getCookieDomain().isBlank()) {
			sb.append("; Domain=").append(cfg.getCookieDomain());
		}
		sb.append("; Max-Age=").append(Math.max(0, maxAgeSec));
		if (cfg.isCookieHttpOnly())
			sb.append("; HttpOnly");
		if (cfg.isCookieSecure())
			sb.append("; Secure");
		String ss = cfg.getCookieSameSite();
		if (ss != null && !ss.isBlank())
			sb.append("; SameSite=").append(ss);
		return sb.toString();
	}

	/** Прочитать произвольный заголовок (null если пусто). */
	public static String readHeader(HttpRequest req, String name) {
		return req.header(name).orElse(null);
	}

	/** Достать Bearer-токен из Authorization. */
	public static String readBearerToken(HttpRequest req) {
		String auth = req.header("Authorization").orElse(null);
		if (auth == null)
			return null;
		if (auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return auth.substring(7);
		}
		return null;
	}
}
