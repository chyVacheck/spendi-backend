
/**
 * @file AuthConfig.java
 * @module config
 *
 * @author Dmytro Shakh
 */

package com.spendi.config;

import com.spendi.core.base.BaseConfig;

/**
 * Конфигурация авторизации/сессий.
 * Источники: .env → System.getenv → дефолты.
 */
public final class AuthConfig extends BaseConfig {
	private static final AuthConfig INSTANCE = new AuthConfig();

	private final String cookieName;
	private final int sessionTtlSec;
	private final boolean cookieSecure;
	private final boolean cookieHttpOnly;
	private final String cookieSameSite; // Lax|Strict|None
	private final String cookiePath;
	private final String cookieDomain; // nullable

	private AuthConfig() {
		this.cookieName = getenv(this.dotenv, "SPENDI_AUTH_COOKIE", "ems.sid");
		this.sessionTtlSec = (int) parseLong(getenv(this.dotenv, "SPENDI_AUTH_TTL_SEC", "36000"), 10 * 3600); // 10h
		this.cookieSecure = parseBool(getenv(this.dotenv, "SPENDI_AUTH_COOKIE_SECURE", "true"), true);
		this.cookieHttpOnly = parseBool(getenv(this.dotenv, "SPENDI_AUTH_COOKIE_HTTP_ONLY", "true"), true);
		this.cookieSameSite = getenv(this.dotenv, "SPENDI_AUTH_COOKIE_SAMESITE", "Lax");
		this.cookiePath = getenv(this.dotenv, "SPENDI_AUTH_COOKIE_PATH", "/");
		this.cookieDomain = getenv(this.dotenv, "SPENDI_AUTH_COOKIE_DOMAIN", null);
	}

	public static AuthConfig getConfig() {
		return INSTANCE;
	}

	public String getCookieName() {
		return cookieName;
	}

	public int getSessionTtlSec() {
		return sessionTtlSec;
	}

	public boolean isCookieSecure() {
		return cookieSecure;
	}

	public boolean isCookieHttpOnly() {
		return cookieHttpOnly;
	}

	public String getCookieSameSite() {
		return cookieSameSite;
	}

	public String getCookiePath() {
		return cookiePath;
	}

	public String getCookieDomain() {
		return cookieDomain;
	}

	@Override
	public String toString() {
		return "AuthConfig{cookie='%s', ttl=%ds, secure=%s, httpOnly=%s, sameSite=%s}".formatted(
				cookieName, sessionTtlSec, cookieSecure, cookieHttpOnly, cookieSameSite);
	}
}
