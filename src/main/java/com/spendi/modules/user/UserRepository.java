
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
* @author Dmytro Shakh
*/

package com.spendi.modules.user;

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
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;

public class UserRepository extends BaseRepository<UserEntity> {

	public static final String COLLECTION = "users";

	public UserRepository(MongoDatabase db) {
		super(UserRepository.class.getSimpleName(), UserEntity.class, db, COLLECTION);
		ensureIndexes();
	}

	/** Уникальный индекс по email в профиле. */
	private void ensureIndexes() {
		collection.createIndex(Indexes.ascending("profile.email"), new IndexOptions().unique(true));
	}

	// =========================
	// ======= MAPPING =========
	// =========================

	@Override
	protected UserEntity toEntity(Document doc) {
		if (doc == null)
			return null;

		UserEntity e = new UserEntity();

		// _id
		ObjectId id = doc.getObjectId("_id");
		e.id = id;

		// profile
		{
			Document d = doc.get("profile", Document.class);
			UserEntity.Profile p = new UserEntity.Profile();
			if (d != null) {
				p.email = d.getString("email");
				p.firstName = d.getString("firstName");
				p.lastName = d.getString("lastName");
				p.avatarFileId = d.getString("avatarFileId");
			}
			e.profile = p;
		}

		// security
		{
			Document d = doc.get("security", Document.class);
			UserEntity.Security s = new UserEntity.Security();
			if (d != null) {
				s.passwordHash = d.getString("passwordHash");
			}
			e.security = s;
		}

		// finance
		{
			Document d = doc.get("finance", Document.class);
			UserEntity.Finance f = new UserEntity.Finance();
			if (d != null) {
				f.defaultAccountId = d.getString("defaultAccountId");
				Object cnt = d.get("accountsCount");
				f.accountsCount = cnt instanceof Number ? ((Number) cnt).intValue() : 0;
				@SuppressWarnings("unchecked")
				List<String> pms = (List<String>) d.get("paymentMethodIds", List.class);
				f.paymentMethodIds = pms;
			}
			e.finance = f;
		}

		// system
		{
			Document d = doc.get("system", Document.class);
			UserEntity.System s = new UserEntity.System();
			if (d != null) {
				Document m = d.get("meta", Document.class);
				UserEntity.Meta meta = new UserEntity.Meta();
				if (m != null) {
					Object ca = m.get("createdAt");
					Object ua = m.get("updatedAt");
					Object la = m.get("lastLoginAt");
					meta.createdAt = toInstant(ca);
					meta.updatedAt = toInstant(ua);
					meta.lastLoginAt = toInstant(la);
				}
				s.meta = meta;
			}
			e.system = s;
		}

		return e;
	}

	@Override
	protected Document toDocument(UserEntity e) {
		Document doc = new Document();

		if (e.id != null) {
			doc.put("_id", e.id);
		}

		// profile
		{
			Document d = new Document();
			if (e.profile != null) {
				d.put("email", e.profile.email);
				d.put("firstName", e.profile.firstName);
				d.put("lastName", e.profile.lastName);
				d.put("avatarFileId", e.profile.avatarFileId);
			}
			doc.put("profile", d);
		}

		// security
		{
			Document d = new Document();
			if (e.security != null) {
				d.put("passwordHash", e.security.passwordHash);
			}
			doc.put("security", d);
		}

		// finance
		{
			Document d = new Document();
			if (e.finance != null) {
				d.put("defaultAccountId", e.finance.defaultAccountId);
				d.put("accountsCount", e.finance.accountsCount);
				d.put("paymentMethodIds",
						e.finance.paymentMethodIds != null ? List.copyOf(e.finance.paymentMethodIds) : List.of());
			}
			doc.put("finance", d);
		}

		// system
		{
			Document d = new Document();
			if (e.system != null) {
				Document m = new Document();
				if (e.system.meta != null) {
					m.put("createdAt",
							e.system.meta.createdAt != null ? Date.from(e.system.meta.createdAt) : null);
					m.put("updatedAt",
							e.system.meta.updatedAt != null ? Date.from(e.system.meta.updatedAt) : null);
					m.put("lastLoginAt",
							e.system.meta.lastLoginAt != null ? Date.from(e.system.meta.lastLoginAt) : null);
				}
				d.put("meta", m);
			}
			doc.put("system", d);
		}

		return doc;
	}

	private static Instant toInstant(Object o) {
		if (o == null)
			return null;
		if (o instanceof Instant i)
			return i;
		if (o instanceof Date d)
			return d.toInstant();
		return null;
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
