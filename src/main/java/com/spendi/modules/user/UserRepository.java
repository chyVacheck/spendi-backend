
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

import java.util.ArrayList;
/**
 * ! java imports
 */
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.core.utils.InstantUtils;
import com.spendi.modules.user.model.UserEntity;
import com.spendi.modules.user.model.UserFinance;
import com.spendi.modules.user.model.UserSystemMeta;
import com.spendi.modules.user.model.UserProfile;
import com.spendi.modules.user.model.UserSecurity;
import com.spendi.modules.user.model.UserSystem;

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

	/**
	 * ? === === === MAPPING === === ===
	 */

	@Override
	protected UserEntity toEntity(Document doc) {
		if (doc == null)
			return null;

		UserEntity e = new UserEntity();

		// _id
		e.setId(doc.getObjectId("_id"));

		// profile
		{
			Document d = doc.get("profile", Document.class);
			UserProfile p = new UserProfile();
			if (d != null) {
				p.setEmail(d.getString("email"));
				p.setFirstName(d.getString("firstName"));
				p.setLastName(d.getString("lastName"));
				p.setAvatarFileId(d.getObjectId("avatarFileId"));
			}
			e.setProfile(p);
		}

		// security
		{
			Document d = doc.get("security", Document.class);
			UserSecurity s = new UserSecurity();
			if (d != null) {
				s.setPasswordHash(d.getString("passwordHash"));
			}
			e.setSecurity(s);
		}

		// finance
		{
			Document d = doc.get("finance", Document.class);
			UserFinance f = new UserFinance();
			if (d != null) {
				f.setDefaultAccountId(d.getObjectId("defaultAccountId"));

				// ожидаем массив ObjectId; но на всякий случай поддержим строки
				@SuppressWarnings("unchecked")
				List<Object> raw = (List<Object>) d.get("paymentMethodIds", List.class);
				if (raw != null) {
					Set<ObjectId> ids = raw.stream().filter(Objects::nonNull)
							.map(o -> (o instanceof ObjectId oid) ? oid : new ObjectId(o.toString()))
							.collect(Collectors.toUnmodifiableSet());
					f.setPaymentMethodIds(ids);
				} else {
					f.setPaymentMethodIds(Set.of());
				}
			}
			e.setFinance(f);
		}

		// system (meta + lastLoginAt в system-уровне)
		{
			Document d = doc.get("system", Document.class);
			UserSystem s = new UserSystem();
			if (d != null) {
				// lastLoginAt хранится на уровне system
				Object lla = d.get("lastLoginAt");
				s.setLastLoginAt(InstantUtils.getInstantOrNull(lla));

				// meta внутри system
				Document m = d.get("meta", Document.class);
				UserSystemMeta meta = new UserSystemMeta();
				if (m != null) {
					meta.setCreatedAt(InstantUtils.getInstantOrNull(m.get("createdAt")));
					meta.setUpdatedAt(InstantUtils.getInstantOrNull(m.get("updatedAt")));
					meta.setDeletedAt(InstantUtils.getInstantOrNull(m.get("deletedAt")));
				}
				s.setMeta(meta);
			}
			e.setSystem(s);
		}

		return e;
	}

	@Override
	protected Document toDocument(UserEntity e) {
		Document doc = new Document();

		doc.put("_id", e.getId());

		// profile
		{
			UserProfile p = e.getProfile();
			Document d = new Document();
			if (p != null) {
				d.put("email", p.getEmail());
				d.put("firstName", p.getFirstName());
				d.put("lastName", p.getLastName());
				d.put("avatarFileId", p.getAvatarFileId());
			}
			doc.put("profile", d);
		}

		// security
		{
			UserSecurity s = e.getSecurity();
			Document d = new Document();
			if (s != null) {
				d.put("passwordHash", s.getPasswordHash());
			}
			doc.put("security", d);
		}

		// finance
		{
			UserFinance f = e.getFinance();
			Document d = new Document();
			if (f != null) {
				d.put("defaultAccountId", f.getDefaultAccountId());
				// в базе храним как список ObjectId
				List<ObjectId> pms = f.getPaymentMethodIds() == null ? List.of()
						: new ArrayList<>(f.getPaymentMethodIds());
				d.put("paymentMethodIds", pms);
			}
			doc.put("finance", d);
		}

		// system (meta + lastLoginAt)
		{
			UserSystem s = e.getSystem();
			Document d = new Document();
			if (s != null) {
				// lastLoginAt на уровне system
				d.put("lastLoginAt", s.getLastLoginAt());

				UserSystemMeta meta = s.getMeta();
				Document m = new Document();
				if (meta != null) {
					m.put("createdAt", meta.getCreatedAt());
					m.put("updatedAt", meta.getUpdatedAt());
					m.put("deletedAt", meta.getDeletedAt());
				}
				d.put("meta", m);
			}
			doc.put("system", d);
		}

		return doc;
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
