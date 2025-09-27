
/**
 * @file UserRepository.java
 * @module modules/user
 *
 * @see BaseRepository
 *
 * Репозиторий для работы с UserEntity.
 * Предоставляет CRUD-операции как в "сыром" виде (Document),
 * так и с маппингом в сущности (TEntity).
 *
 * Строгий маппинг через req/opt хелперы BaseRepository.
 *
 * @author Dmytro Shakh
 */

package com.spendi.modules.user;

/**
 * ! lib imports
 */
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

/**
 * ! java imports
 */
import java.util.Optional;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.modules.user.model.UserEntity;

public class UserRepository extends BaseRepository<UserEntity> {

	public static final String COLLECTION = "users";

	public UserRepository(MongoDatabase db) {
		super(UserRepository.class.getSimpleName(), UserEntity.class, db, COLLECTION, UserMapper.getInstance());
		ensureIndexes();
	}

	/** Уникальный индекс по email в профиле. */
	private void ensureIndexes() {
		collection.createIndex(Indexes.ascending("profile.email"), new IndexOptions().unique(true));
	}

	// =========================
	// ====== SHORTCUTS ========
	// =========================

	/** Найти по email. */
	public Optional<UserEntity> findByEmail(String email) {
		return this.findOne("profile.email", email);
	}

	/** Проверить существование по email. */
	public boolean existsByEmail(String email) {
		return this.exists("profile.email", email);
	}
}
