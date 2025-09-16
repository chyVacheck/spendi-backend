/**
 * @file SessionService.java
 * @module modules/session
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
import com.spendi.core.base.database.MongoUpdateBuilder;
import com.spendi.core.base.service.BaseRepositoryService;
import com.spendi.core.response.ServiceResponse;
import com.spendi.modules.session.dto.CreateSessionDto;
import com.spendi.core.exceptions.UnauthorizedException;

public class SessionService extends BaseRepositoryService<SessionRepository, SessionEntity> {

	private static volatile SessionService INSTANCE;
	private final AuthConfig authCfg = AuthConfig.getConfig();

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

	/**
	 * ? === === === Create === === ===
	 */

	/**
	 * Создать сессию.
	 * 
	 * @param requestId request-id для корреляции логов
	 * 
	 * @param dto       данные для создания сессии
	 * 
	 * @return созданная сессия
	 */
	public ServiceResponse<SessionEntity> create(String requestId, CreateSessionDto dto) {
		var s = new SessionEntity();
		s.id = new ObjectId();
		s.userId = new ObjectId(dto.getUserId());
		s.createdAt = Instant.now();
		s.lastSeenAt = s.createdAt;
		s.expiresAt = s.createdAt.plusSeconds(authCfg.getSessionTtlSec());
		s.revoked = false;
		s.ip = dto.getIp();
		s.userAgent = dto.getUserAgent();
		var created = super.createOne(s);
		// Лог: создана сессия
		this.info("session created", requestId, detailsOf("sessionId", s.id.toHexString(), "expiresAt",
				s.expiresAt.toString(), "userId", dto.getUserId()), true);
		return created;
	}

	/**
	 * ? === === === Read === === ===
	 */

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

	/**
	 * ? === === === Update === === ===
	 */

	/**
	 * Обновить время последнего посещения сессии.
	 * 
	 * @param requestId request-id для корреляции логов
	 * 
	 * @throws EntityNotFoundException если сессия не найдена, в теории никогда не должно быть
	 * 
	 * @param id строковый ObjectId сессии
	 */
	public ServiceResponse<SessionEntity> touch(String requestId, String id) {
		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.currentDate("lastSeenAt");
		updateBuilder.set("expiresAt", Instant.now().plusSeconds(authCfg.getSessionTtlSec()));
		// Лог: обновлено время последнего посещения
		this.info("session touched", requestId, detailsOf("sessionId", id));
		return this.updateById(id, updateBuilder.build());
	}

	public ServiceResponse<String> revokeById(String requestId, String id) {
		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.set("revoked", true);

		this.updateById(id, updateBuilder.build());

		// Лог: сессия отозвана
		this.info("session revoked", requestId, detailsOf("sessionId", id), true);
		return ServiceResponse.deleted(id);
	}

	/**
	 * Отозвать все активные сессии пользователя (revoked=false). Не трогаем уже отозванные сессии, чтобы сохранялась
	 * история.
	 *
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @return количество затронутых сессий
	 */
	public ServiceResponse<Long> revokeActiveByUser(String requestId, String userId) {
		var filter = Map.<String, Object>of("userId", new ObjectId(userId), "revoked", false);

		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.set("revoked", true);

		var res = this.updateMany(filter, updateBuilder.build());

		// Логируем количество закрытых сессий
		this.info("sessions revoked by userId", requestId,
				detailsOf("userId", userId, "count", String.valueOf(res.getData())), true);
		return res;
	}
}
