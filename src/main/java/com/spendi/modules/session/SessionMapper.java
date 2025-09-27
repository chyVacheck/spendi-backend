/**
 * @file SessionMapper.java
 * @module modules/session
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.session;

/**
 * ! lib imports
 */
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMapper;
import com.spendi.core.types.DocMapper;

/**
 * Репозиторий для работы с сущностями пользовательских сессий {@link SessionEntity}. Предоставляет методы для
 * сохранения, получения и управления сессиями в базе данных MongoDB.
 */
public class SessionMapper extends BaseMapper<SessionEntity> implements DocMapper<SessionEntity> {

	/**
	 * Экземпляр {@link SessionMapper}.
	 */
	private static final SessionMapper INSTANCE = new SessionMapper();

	public SessionMapper() {
		super(SessionMapper.class.getSimpleName(), SessionEntity.class, "sessions");
	}

	/**
	 * Получает экземпляр {@link SessionMapper}.
	 *
	 * @return Экземпляр {@link SessionMapper}.
	 */
	public static SessionMapper getInstance() {
		return INSTANCE;
	}

	/**
	 * ? === === === MAPPING === === ===
	 */

	/**
	 * Преобразует документ MongoDB в сущность {@link SessionEntity}.
	 *
	 * @param doc Документ MongoDB.
	 * @return Сущность {@link SessionEntity}, или null, если входной документ null.
	 */
	@Override
	public SessionEntity toEntity(Document doc) {
		if (doc == null)
			return null;

		ObjectId id = reqObjectId(doc, "_id", null);
		ObjectId userId = reqObjectId(doc, "userId", id);

		var createdAt = reqInstant(doc, "createdAt", id);
		var expiresAt = reqInstant(doc, "expiresAt", id);
		var lastSeenAt = optInstant(doc, "lastSeenAt").orElse(null);

		var revoked = reqBoolean(doc, "revoked", id);
		var ip = optString(doc, "ip").orElse(null);
		var userAgent = optString(doc, "userAgent").orElse(null);

		return SessionEntity.builder().id(id).userId(userId).createdAt(createdAt).expiresAt(expiresAt)
				.lastSeenAt(lastSeenAt).revoked(revoked).ip(ip).userAgent(userAgent).build();
	}

	/**
	 * Преобразует сущность {@link SessionEntity} в документ MongoDB.
	 *
	 * @param e Сущность {@link SessionEntity}.
	 * @return Документ MongoDB.
	 * @throws IllegalArgumentException если обязательные поля сущности отсутствуют.
	 */
	@Override
	public Document toDocument(SessionEntity e) {
		if (e == null)
			return new Document();

		// обязательные поля
		if (e.getId() == null)
			missing("ObjectId", "_id", null);
		if (e.getUserId() == null)
			missing("ObjectId", "userId", e.getId());
		if (e.getCreatedAt() == null)
			missing("Instant", "createdAt", e.getId());
		if (e.getExpiresAt() == null)
			missing("Instant", "expiresAt", e.getId());

		Document d = new Document();
		d.put("_id", e.getId());
		d.put("userId", e.getUserId());
		d.put("createdAt", e.getCreatedAt());
		d.put("lastSeenAt", e.getLastSeenAt());
		d.put("expiresAt", e.getExpiresAt());
		d.put("revoked", e.isRevoked()); // primitive boolean — всегда пишем
		d.put("ip", e.getIp());
		d.put("userAgent", e.getUserAgent());
		return d;
	}

}
