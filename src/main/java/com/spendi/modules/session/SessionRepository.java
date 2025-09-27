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
		super(SessionRepository.class.getSimpleName(), SessionEntity.class, db, COLLECTION,
				SessionMapper.getInstance());
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
