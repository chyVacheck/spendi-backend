/**
 * @file SessionRepository.java
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
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

/**
 * ! java imports
 */
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;

/**
 * Репозиторий для работы с сущностями пользовательских сессий {@link SessionEntity}. Предоставляет методы для
 * сохранения, получения и управления сессиями в базе данных MongoDB.
 */
public class SessionRepository extends BaseRepository<SessionEntity> {

	/**
	 * Название коллекции в базе данных для хранения сессий.
	 */
	public static final String COLLECTION = "sessions";

	/**
	 * Конструктор репозитория сессий.
	 *
	 * @param db Экземпляр {@link MongoDatabase} для взаимодействия с базой данных.
	 */
	public SessionRepository(MongoDatabase db) {
		super(SessionRepository.class.getSimpleName(), SessionEntity.class, db, COLLECTION);
		ensureIndexes();
	}

	/**
	 * Обеспечивает наличие необходимых индексов для коллекции сессий. Включает TTL-индекс для автоматического удаления
	 * просроченных сессий и индексы для полей userId и revoked для оптимизации запросов.
	 */
	private void ensureIndexes() {
		// TTL на expiresAt
		collection.createIndex(Indexes.ascending("expiresAt"), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
		// полезные индексы
		collection.createIndex(Indexes.ascending("userId"));
		collection.createIndex(Indexes.ascending("revoked"));
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
	protected SessionEntity toEntity(Document doc) {
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
	protected Document toDocument(SessionEntity e) {
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

	/**
	 * Находит активную сессию по её hex-идентификатору.
	 *
	 * @param id идентификатор сессии
	 * @return активная сессия, если она существует
	 */
	public Optional<SessionEntity> findActiveById(String idHex) {
		ObjectId id = new ObjectId(idHex);
		return this.findOne("_id", id).filter(s -> s.isActive());
	}
}
