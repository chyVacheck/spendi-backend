
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.core.utils.InstantUtils;

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
		e.setId(id);

		// profile
		{
			Document d = doc.get("profile", Document.class);
			UserEntity.Profile p = new UserEntity.Profile();
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
			UserEntity.Security s = new UserEntity.Security();
			if (d != null) {
				s.setPasswordHash(d.getString("passwordHash"));
			}
			e.setSecurity(s);
		}

		// finance
		{
			Document d = doc.get("finance", Document.class);
			UserEntity.Finance f = new UserEntity.Finance();
			if (d != null) {
				f.setDefaultAccountId(d.getObjectId("defaultAccountId"));
				@SuppressWarnings("unchecked")
				List<String> pms = (List<String>) d.get("paymentMethodIds", List.class);
				f.setPaymentMethodIds(pms.stream().map(ObjectId::new).collect(Collectors.toSet()));
			}
			e.setFinance(f);
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
					meta.setCreatedAt(InstantUtils.getInstantOrNull(ca));
					meta.setUpdatedAt(InstantUtils.getInstantOrNull(ua));
					meta.setLastLoginAt(InstantUtils.getInstantOrNull(la));
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
			Document d = new Document();

			d.put("email", e.getProfile().getEmail());
			d.put("firstName", e.getProfile().getFirstName());
			d.put("lastName", e.getProfile().getLastName());
			d.put("avatarFileId", e.getProfile().getAvatarFileId());

			doc.put("profile", d);
		}

		// security
		{
			Document d = new Document();

			d.put("passwordHash", e.getSecurity().getPasswordHash());

			doc.put("security", d);
		}

		// finance
		{
			Document d = new Document();

			d.put("defaultAccountId", e.getFinance().getDefaultAccountId());
			d.put("paymentMethodIds", List.copyOf(e.getFinance().getPaymentMethodIds())); // todo set

			doc.put("finance", d);
		}

		// system
		{
			Document d = new Document();
			// meta
			UserEntity.Meta meta = e.getSystem().getMeta();
			Document m = new Document();
			m.put("createdAt", meta.getCreatedAt());
			m.put("updatedAt", meta.getUpdatedAt());
			m.put("lastLoginAt", meta.getLastLoginAt());
			d.put("meta", m);

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
