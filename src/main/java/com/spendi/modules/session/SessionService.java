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
import com.spendi.modules.session.cmd.SessionCreateCmd;
import com.spendi.core.exceptions.UnauthorizedException;

/**
 * Сервис для управления пользовательскими сессиями. Предоставляет методы для создания, получения, обновления и отзыва
 * сессий.
 */
public class SessionService extends BaseRepositoryService<SessionRepository, SessionEntity> {

	/**
	 * Единственный экземпляр {@link SessionService} (Singleton).
	 */
	private static volatile SessionService INSTANCE;
	/**
	 * Конфигурация аутентификации, используемая для определения времени жизни сессий.
	 */
	private final AuthConfig authCfg = AuthConfig.getConfig();

	/**
	 * Конструктор сервиса сессий.
	 *
	 * @param repository Репозиторий для доступа к данным сессий.
	 */
	public SessionService(SessionRepository repository) {
		super(SessionService.class.getSimpleName(), repository);
	}

	/**
	 * Инициализирует единственный экземпляр {@link SessionService}. Этот метод должен быть вызван один раз при старте
	 * приложения.
	 *
	 * @param repository Репозиторий для доступа к данным сессий.
	 */
	public static void init(SessionRepository repository) {
		synchronized (SessionService.class) {
			if (INSTANCE == null) {
				INSTANCE = new SessionService(repository);
			}
		}
	}

	/**
	 * Возвращает единственный экземпляр {@link SessionService}.
	 *
	 * @return Экземпляр {@link SessionService}.
	 * @throws IllegalStateException если сервис не был инициализирован.
	 */
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
	public ServiceResponse<SessionEntity> create(String requestId, SessionCreateCmd cmd) {
		var s = new SessionEntity();
		s.setId(new ObjectId());
		s.setUserId(new ObjectId(cmd.getUserId()));
		s.setCreatedAt(Instant.now());
		s.setLastSeenAt(s.getCreatedAt());
		s.setExpiresAt(s.getCreatedAt().plusSeconds(authCfg.getSessionTtlSec()));
		s.setRevoked(false);
		s.setIp(cmd.getIp());
		s.setUserAgent(cmd.getUserAgent());
		var created = super.createOne(s);
		// Лог: создана сессия
		this.info("session created", requestId, detailsOf("sessionId", s.getHexId(), "expiresAt",
				s.getExpiresAt().toString(), "userId", cmd.getUserId()), true);
		return created;
	}

	/**
	 * ? === === === Read === === ===
	 */

	/**
	 * Найти активную (не отозванную и не истёкшую) сессию по id.
	 *
	 * @param id Идентификатор сессии в виде строки.
	 * @return {@link ServiceResponse} с найденной активной сессией.
	 * @throws UnauthorizedException если сессия не найдена или неактивна.
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

	/**
	 * Отзывает сессию по её идентификатору.
	 *
	 * @param requestId Идентификатор запроса для логирования.
	 * @param id        Идентификатор сессии, которую нужно отозвать.
	 * @return {@link ServiceResponse} с hex-строковым представлением отозванной сессии.
	 */
	public ServiceResponse<String> revokeById(String requestId, ObjectId id) {
		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.set("revoked", true);

		this.updateById(id, updateBuilder.build());

		// Лог: сессия отозвана
		this.info("session revoked", requestId, detailsOf("sessionId", id.toHexString()), true);
		return ServiceResponse.deleted(id.toHexString());
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
