
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
import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

/**
 * ! java imports
 */
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.modules.user.model.UserEntity;
import com.spendi.modules.user.model.UserFinance;
import com.spendi.modules.user.model.UserProfile;
import com.spendi.modules.user.model.UserSecurity;
import com.spendi.modules.user.model.UserSystem;
import com.spendi.modules.user.model.UserSystemMeta;

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

		// id
		ObjectId id = reqObjectId(doc, "_id", null);

		// profile (required block)
		Document pDoc = reqSubDoc(doc, "profile", id);
		UserProfile profile = new UserProfile();
		profile.setEmail(reqString(pDoc, "email", id));
		profile.setFirstName(optString(pDoc, "firstName").orElse(null));
		profile.setLastName(optString(pDoc, "lastName").orElse(null));
		profile.setAvatarFileId(optObjectId(pDoc, "avatarFileId").orElse(null));

		// security (required block)
		Document sDoc = reqSubDoc(doc, "security", id);
		UserSecurity security = new UserSecurity();
		security.setPasswordHash(reqString(sDoc, "passwordHash", id));

		// finance (required block)
		Document fDoc = reqSubDoc(doc, "finance", id);
		UserFinance finance = new UserFinance();
		finance.setDefaultAccountId(optObjectId(fDoc, "defaultAccountId").orElse(null));

		// paymentMethodIds: допускаем отсутствие поля → пустой Set
		List<ObjectId> pmList = fDoc.containsKey("paymentMethodIds") ? reqObjectIdList(fDoc, "paymentMethodIds", id)
				: List.of();
		finance.setPaymentMethodIds(Set.copyOf(pmList));

		// system (required block)
		Document sysDoc = reqSubDoc(doc, "system", id);
		UserSystem system = new UserSystem();
		system.setLastLoginAt(optInstant(sysDoc, "lastLoginAt").orElse(null));

		// meta (required sub-block)
		Document mDoc = reqSubDoc(sysDoc, "meta", id, "system.meta");
		UserSystemMeta meta = new UserSystemMeta();
		meta.setCreatedAt(reqInstant(mDoc, "createdAt", id));
		meta.setUpdatedAt(optInstant(mDoc, "updatedAt").orElse(null));
		meta.setDeletedAt(optInstant(mDoc, "deletedAt").orElse(null));
		system.setMeta(meta);

		// assemble
		UserEntity e = new UserEntity();
		e.setId(id);
		e.setProfile(profile);
		e.setSecurity(security);
		e.setFinance(finance);
		e.setSystem(system);

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
