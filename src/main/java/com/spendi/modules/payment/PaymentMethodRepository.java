/**
 * @file PaymentMethodRepository.java
 * @module modules/payment
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment;

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

import com.spendi.core.base.BaseRepository;

public class PaymentMethodRepository extends BaseRepository<PaymentMethodEntity> {

	public static final String COLLECTION = "payment_methods";

	public PaymentMethodRepository(MongoDatabase db) {
		super(PaymentMethodRepository.class.getSimpleName(), PaymentMethodEntity.class, db, COLLECTION);
		ensureIndexes();
	}

	private void ensureIndexes() {
		// Поиск по userId частый
		collection.createIndex(Indexes.ascending("userId"));
		// Условная уникальность имени в рамках пользователя (не строго обязательно)
		collection.createIndex(Indexes.ascending("userId", "info.name"), new IndexOptions().unique(false));
	}

	@Override
	protected PaymentMethodEntity toEntity(Document doc) {
		if (doc == null)
			return null;
		PaymentMethodEntity e = new PaymentMethodEntity();
		e.id = doc.getObjectId("_id");
		ObjectId uid = doc.getObjectId("userId");
		e.userId = uid;

		// info
		{
			Document d = doc.get("info", Document.class);
			PaymentMethodEntity.Info i = new PaymentMethodEntity.Info();
			if (d != null) {
				String t = d.getString("type");
				if (t != null)
					i.type = PaymentMethodEntity.EPaymentMethodType.valueOf(t);
				i.name = d.getString("name");
				i.currency = d.getString("currency");
				Object o = d.get("order");
				i.order = (o instanceof Number) ? ((Number) o).intValue() : null;
				@SuppressWarnings("unchecked")
				List<String> tags = (List<String>) d.get("tags", List.class);
				i.tags = tags;
			}
			e.info = i;
		}

		// details
		{
			Document d = doc.get("details", Document.class);
			PaymentMethodEntity.Details det = new PaymentMethodEntity.Details();
			if (d != null) {
				Document c = d.get("card", Document.class);
				if (c != null) {
					PaymentMethodEntity.Card card = new PaymentMethodEntity.Card();
					card.brand = c.getString("brand");
					card.last4 = c.getString("last4");
					Object em = c.get("expMonth");
					Object ey = c.get("expYear");
					card.expMonth = em instanceof Number ? ((Number) em).intValue() : null;
					card.expYear = ey instanceof Number ? ((Number) ey).intValue() : null;
					det.card = card;
				}
				Document b = d.get("bank", Document.class);
				if (b != null) {
					PaymentMethodEntity.Bank bank = new PaymentMethodEntity.Bank();
					bank.bankName = b.getString("bankName");
					bank.accountMasked = b.getString("accountMasked");
					det.bank = bank;
				}
				Document w = d.get("wallet", Document.class);
				if (w != null) {
					PaymentMethodEntity.Wallet wallet = new PaymentMethodEntity.Wallet();
					wallet.provider = w.getString("provider");
					wallet.handle = w.getString("handle");
					det.wallet = wallet;
				}
			}
			e.details = det;
		}

		// system
		{
			Document d = doc.get("system", Document.class);
			PaymentMethodEntity.System s = new PaymentMethodEntity.System();
			if (d != null) {
				String st = d.getString("status");
				if (st != null)
					s.status = PaymentMethodEntity.EPaymentMethodStatus.valueOf(st);
				Document m = d.get("meta", Document.class);
				PaymentMethodEntity.Meta meta = new PaymentMethodEntity.Meta();
				if (m != null) {
					Object ca = m.get("createdAt");
					Object ua = m.get("updatedAt");
					Object aa = m.get("archivedAt");
					meta.createdAt = (ca instanceof Date) ? ((Date) ca).toInstant()
							: (ca instanceof Instant ? (Instant) ca : null);
					meta.updatedAt = (ua instanceof Date) ? ((Date) ua).toInstant()
							: (ua instanceof Instant ? (Instant) ua : null);
					meta.archivedAt = (aa instanceof Date) ? ((Date) aa).toInstant()
							: (aa instanceof Instant ? (Instant) aa : null);
				}
				s.meta = meta;
			}
			e.system = s;
		}

		return e;
	}

	@Override
	protected Document toDocument(PaymentMethodEntity e) {
		Document doc = new Document();
		if (e.id != null)
			doc.put("_id", e.id);
		if (e.userId != null)
			doc.put("userId", e.userId);

		// info
		{
			Document d = new Document();
			if (e.info != null) {
				d.put("type", e.info.type != null ? e.info.type.name() : null);
				d.put("name", e.info.name);
				d.put("currency", e.info.currency);
				d.put("order", e.info.order);
				d.put("tags", e.info.tags);
			}
			doc.put("info", d);
		}

		// details
		{
			Document d = new Document();
			if (e.details != null) {
				if (e.details.card != null) {
					Document c = new Document();
					c.put("brand", e.details.card.brand);
					c.put("last4", e.details.card.last4);
					c.put("expMonth", e.details.card.expMonth);
					c.put("expYear", e.details.card.expYear);
					d.put("card", c);
				}
				if (e.details.bank != null) {
					Document b = new Document();
					b.put("bankName", e.details.bank.bankName);
					b.put("accountMasked", e.details.bank.accountMasked);
					d.put("bank", b);
				}
				if (e.details.wallet != null) {
					Document w = new Document();
					w.put("provider", e.details.wallet.provider);
					w.put("handle", e.details.wallet.handle);
					d.put("wallet", w);
				}
			}
			doc.put("details", d);
		}

		// system
		{
			Document d = new Document();
			if (e.system != null) {
				d.put("status", e.system.status != null ? e.system.status.name() : null);
				Document m = new Document();
				if (e.system.meta != null) {
					m.put("createdAt", e.system.meta.createdAt != null ? Date.from(e.system.meta.createdAt) : null);
					m.put("updatedAt", e.system.meta.updatedAt != null ? Date.from(e.system.meta.updatedAt) : null);
					m.put("archivedAt", e.system.meta.archivedAt != null ? Date.from(e.system.meta.archivedAt) : null);
				}
				d.put("meta", m);
			}
			doc.put("system", d);
		}

		return doc;
	}

	public List<PaymentMethodEntity> findByUserId(String userId, int page, int limit) {
		return this.findMany("userId", new ObjectId(userId), page, limit);
	}
}
