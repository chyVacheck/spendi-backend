/**
 * @file SessionService.java
 * @module modules/auth
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.session;

/**
 * ! lin imports
 */
import org.bson.types.ObjectId;

/**
 * ! java imports
 */
import java.time.Instant;
import java.util.Map;

/**
 * ! my imports
 */
import com.spendi.config.AuthConfig;
import com.spendi.core.base.service.BaseRepositoryService;
import com.spendi.core.response.ServiceResponse;
import com.spendi.core.exceptions.UnauthorizedException;

public class SessionService extends BaseRepositoryService<SessionRepository, SessionEntity> {

	private static volatile SessionService INSTANCE;
	private final AuthConfig authCfg = new AuthConfig();

	public SessionService(SessionRepository repository) {
		super(SessionService.class.getSimpleName(), repository);
	}

	public static void init(SessionRepository repository) {
		synchronized (SessionService.class) {
			if (INSTANCE == null) {
				INSTANCE = new SessionService(repository);
			}
		}
	}

	public static SessionService getInstance() {
		SessionService ref = INSTANCE;
		if (ref == null)
			throw new IllegalStateException("SessionService not initialized. Call AppInitializer.initAll()");
		return ref;
	}

	public ServiceResponse<SessionEntity> create(String requestId, String userId, String ip, String ua) {
		var s = new SessionEntity();
		s.id = new ObjectId();
		s.userId = new ObjectId(userId);
		s.createdAt = Instant.now();
		s.lastSeenAt = s.createdAt;
		s.expiresAt = s.createdAt.plusSeconds(authCfg.getSessionTtlSec());
		s.revoked = false;
		s.ip = ip;
		s.userAgent = ua;
		var created = super.createOne(s);
		// Лог: создана сессия
		this.info("session created", requestId,
				detailsOf("sessionId", s.id.toHexString(), "userId", userId), true);
		return created;
	}

	public ServiceResponse<SessionEntity> touch(String id) {
		var now = Instant.now();
		var updates = Map.<String, Object>of(
				"lastSeenAt", java.util.Date.from(now));
		return this.updateById(id, updates);
	}

	/**
	 * Найти активную (не отозванную и не истёкшую) сессию по id.
	 */
	public ServiceResponse<SessionEntity> getActiveById(String id) {
		var opt = this.repository.findActiveById(id);
		if (opt.isEmpty()) {
			throw new UnauthorizedException("Session is invalid or expired", Map.of("sessionId", id));
		}
		return ServiceResponse.founded(opt.get());
	}

	public ServiceResponse<String> revokeById(String requestId, String id) {
		var updates = Map.<String, Object>of("revoked", true);
		this.updateById(id, updates);
		// Лог: сессия отозвана
		this.info("session revoked", requestId, detailsOf("sessionId", id), true);
		return ServiceResponse.deleted(id);
	}

	/**
	 * Отозвать все активные сессии пользователя (revoked=false).
	 * Не трогаем уже отозванные сессии, чтобы сохранялась история.
	 *
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @return количество затронутых сессий
	 */
	public ServiceResponse<Long> revokeActiveByUser(String requestId, String userId) {
		var filter = Map.<String, Object>of(
				"userId", new ObjectId(userId),
				"revoked", false);
		var updates = Map.<String, Object>of("revoked", true);
		var res = this.updateMany(filter, updates);
		// Логируем количество закрытых сессий
		this.info("sessions revoked by userId", requestId,
				detailsOf("userId", userId, "count", String.valueOf(res.getData())), true);
		return res;
	}
}
