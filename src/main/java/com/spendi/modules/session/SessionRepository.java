/**
 * @file SessionRepository.java
 * @module modules/auth
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
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;

public class SessionRepository extends BaseRepository<SessionEntity> {

	public static final String COLLECTION = "sessions";

	public SessionRepository(MongoDatabase db) {
		super(SessionRepository.class.getSimpleName(), SessionEntity.class, db, COLLECTION);
		ensureIndexes();
	}

	private void ensureIndexes() {
		// TTL на expiresAt
		collection.createIndex(Indexes.ascending("expiresAt"), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
		// полезные индексы
		collection.createIndex(Indexes.ascending("userId"));
		collection.createIndex(Indexes.ascending("revoked"));
	}

	@Override
	protected SessionEntity toEntity(Document doc) {
		if (doc == null)
			return null;
		SessionEntity s = new SessionEntity();
		s.id = doc.getObjectId("_id");
		s.userId = doc.getObjectId("userId");
		Object ca = doc.get("createdAt");
		Object la = doc.get("lastSeenAt");
		Object ea = doc.get("expiresAt");
		s.createdAt = toInstant(ca);
		s.lastSeenAt = toInstant(la);
		s.expiresAt = toInstant(ea);
		Object rev = doc.get("revoked");
		s.revoked = rev instanceof Boolean ? (Boolean) rev : false;
		s.ip = doc.getString("ip");
		s.userAgent = doc.getString("userAgent");
		return s;
	}

	@Override
	protected Document toDocument(SessionEntity e) {
		Document d = new Document();
		if (e.id != null)
			d.put("_id", e.id);
		if (e.userId != null)
			d.put("userId", e.userId);
		d.put("createdAt", e.createdAt != null ? Date.from(e.createdAt) : null);
		d.put("lastSeenAt", e.lastSeenAt != null ? Date.from(e.lastSeenAt) : null);
		d.put("expiresAt", e.expiresAt != null ? Date.from(e.expiresAt) : null);
		d.put("revoked", e.revoked);
		d.put("ip", e.ip);
		d.put("userAgent", e.userAgent);
		return d;
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

	public Optional<SessionEntity> findActiveById(String id) {
		return this.findOne("_id", new ObjectId(id))
				.filter(s -> !s.revoked && (s.expiresAt == null || s.expiresAt.isAfter(Instant.now())));
	}
}
