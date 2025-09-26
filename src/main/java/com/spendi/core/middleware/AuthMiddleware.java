/**
 * @file AuthMiddleware.java
 * @module core/middleware
 *
 * Миддлвара аутентификации: читает cookie с id сессии, валидирует активную
 * сессию, загружает пользователя и кладёт оба объекта в контекст запроса под
 * ключами RequestAttr.AUTH_SESSION.
 */

package com.spendi.core.middleware;

/**
 * ! java imports
 */
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.config.AuthConfig;
import com.spendi.core.base.BaseMiddleware;
import com.spendi.core.base.http.HttpContext;
import com.spendi.core.base.http.MiddlewareChain;
import com.spendi.core.base.http.RequestAttr;
import com.spendi.core.exceptions.UnauthorizedException;
import com.spendi.core.utils.CookieUtils;
import com.spendi.modules.session.SessionEntity;
import com.spendi.modules.session.SessionService;

public final class AuthMiddleware extends BaseMiddleware {

	private final static AuthMiddleware INSTANCE = new AuthMiddleware();

	private final AuthConfig authCfg = AuthConfig.getConfig();
	private final SessionService sessionService = SessionService.getInstance();

	protected AuthMiddleware() {
		super(AuthMiddleware.class.getSimpleName());
	}

	public static AuthMiddleware getInstance() {
		return INSTANCE;
	}

	@Override
	public void handle(HttpContext ctx, MiddlewareChain chain) throws Exception {
		String sid = CookieUtils.readCookie(ctx.req(), authCfg.getCookieName());
		if (sid == null || sid.isBlank()) {
			throw new UnauthorizedException("Auth cookie is missing", Map.of("cookie", authCfg.getCookieName()));
		}

		// Найти активную сессию (проверяет revoked и expiresAt)
		SessionEntity s = this.sessionService.getActiveById(sid).getData();

		// Опционально обновим lastSeenAt
		try {
			this.sessionService.touch(ctx.getRequestId(), s.getHexId());
		} catch (RuntimeException ignore) {
		}

		this.info("Session id founded", ctx.getRequestId(), detailsOf("sessionId", sid, "userId", s.getUserHexId()));

		// Положить в контекст для следующих хэндлеров/мидлвар
		ctx.setAttr(RequestAttr.AUTH_SESSION, s);

		chain.next();
	}
}
