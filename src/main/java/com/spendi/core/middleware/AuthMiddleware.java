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

public class AuthMiddleware extends BaseMiddleware {

	private final static AuthMiddleware INSTANCE = new AuthMiddleware();

	private final AuthConfig authCfg = new AuthConfig();
	private final SessionService sessions = SessionService.getInstance();

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
			throw new UnauthorizedException("Auth cookie is missing",
					java.util.Map.of("cookie", authCfg.getCookieName()));
		}

		// Найти активную сессию (проверяет revoked и expiresAt)
		SessionEntity s = sessions.getActiveById(sid).getData();

		// Опционально обновим lastSeenAt
		try {
			sessions.touch(s.id.toHexString());
		} catch (RuntimeException ignore) {
		}

		this.info("Session id founded", ctx.getRequestId(),
				detailsOf("sessionId", sid, "userId", s.userId.toHexString()));

		// Положить в контекст для следующих хэндлеров/мидлвар
		ctx.setAttr(RequestAttr.AUTH_SESSION, s);

		chain.next();
	}
}
