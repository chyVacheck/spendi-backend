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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseRepository;
import com.spendi.modules.payment.model.BankDetails;
import com.spendi.modules.payment.model.CardDetails;
import com.spendi.modules.payment.model.PaymentMethodDetails;
import com.spendi.modules.payment.model.EPaymentMethodStatus;
import com.spendi.modules.payment.model.EPaymentMethodType;
import com.spendi.modules.payment.model.PaymentMethodEntity;
import com.spendi.modules.payment.model.PaymentMethodInfo;
import com.spendi.modules.payment.model.WalletDetails;
import com.spendi.modules.payment.model.PaymentMethodSystem;
import com.spendi.shared.mapper.EntityMetaMapper;
import com.spendi.shared.model.EntityMeta;

public class PaymentMethodRepository extends BaseRepository<PaymentMethodEntity> {

	public static final String COLLECTION = "payment_methods";
	private final static EntityMetaMapper metaMapper = EntityMetaMapper.getInstance();

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

	/**
	 * ? === === === MAPPING === === ===
	 */

	@Override
	protected PaymentMethodEntity toEntity(Document doc) {
		PaymentMethodEntity e = new PaymentMethodEntity();
		e.setId(doc.getObjectId("_id"));
		e.setUserId(doc.getObjectId("userId"));

		// --- info ---
		Document di = doc.get("info", Document.class);
		PaymentMethodInfo info = new PaymentMethodInfo();
		String t = di.getString("type");
		info.setType(EPaymentMethodType.valueOf(t));
		info.setName(di.getString("name"));
		info.setCurrency(di.getString("currency"));
		info.setOrder(di.getInteger("order"));
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>) di.get("tags", List.class);
		info.setTags(Set.copyOf(tags));
		e.setInfo(info);

		// --- details ---

		Document dd = doc.get("details", Document.class);
		e.setDetails(parseDetailsByKind(info.getType(), dd));

		// --- system ---
		{
			Document ds = doc.get("system", Document.class);
			PaymentMethodSystem sys = new PaymentMethodSystem();

			String st = ds.getString("status");

			try {
				sys.setStatus(EPaymentMethodStatus.valueOf(st));
			} catch (IllegalArgumentException ignore) {
				// статус останется null
				this.warn("Unknown payment method status", "no-id", Map.of("status", st));
			}

			Document dm = ds.get("meta", Document.class);
			sys.setMeta(metaMapper.fromDocument(dm));

			e.setSystem(sys);
		}

		return e;
	}

	@Override
	protected Document toDocument(PaymentMethodEntity e) {
		Document doc = new Document();
		doc.put("_id", e.getId());
		doc.put("userId", e.getUserId());

		// --- info ---
		{
			Document di = new Document();
			PaymentMethodInfo i = e.getInfo();
			di.put("type", i.getType() != null ? i.getType().name() : null);
			di.put("name", i.getName());
			di.put("currency", i.getCurrency());
			di.put("order", i.getOrder());
			di.put("tags", List.copyOf(i.getTags()));
			doc.put("info", di);
		}

		// --- details ---
		{
			Document dd = new Document();
			PaymentMethodDetails det = e.getDetails();
			if (det instanceof CardDetails c) {
				dd.put("kind", "CARD");
				dd.put("brand", c.getBrand());
				dd.put("last4", c.getLast4());
				dd.put("expMonth", c.getExpMonth());
				dd.put("expYear", c.getExpYear());
			} else if (det instanceof BankDetails b) {
				dd.put("kind", "BANK");
				dd.put("bankName", b.getBankName());
				dd.put("accountMasked", b.getAccountMasked());
			} else if (det instanceof WalletDetails w) {
				dd.put("kind", "WALLET");
				dd.put("provider", w.getProvider());
				dd.put("handle", w.getHandle());
			} else {
				// Если details отсутствует — не кладём поле вовсе, чтобы не плодить пустые документы
				dd = null;
			}

			doc.put("details", dd);

		}

		// --- system ---
		{
			Document ds = new Document();
			PaymentMethodSystem s = e.getSystem();
			if (s != null) {
				ds.put("status", s.getStatus() != null ? s.getStatus().name() : null);

				EntityMeta meta = s.getMeta();
				ds.put("meta", metaMapper.toDocument(meta));

			}
			doc.put("system", ds);
		}

		return doc;
	}

	public List<PaymentMethodEntity> findByUserId(String userId, int page, int limit) {
		return this.findMany("userId", new ObjectId(userId), page, limit);
	}

	private PaymentMethodDetails parseDetailsByKind(EPaymentMethodType type, Document d) {
		return switch (type) {
		case CARD -> CardDetails.builder().brand(d.getString("brand")).last4(d.getString("last4"))
				.expMonth(d.getInteger("expMonth")).expYear(d.getInteger("expYear")).build();
		case BANK -> BankDetails.builder().bankName(d.getString("bankName")).accountMasked(d.getString("accountMasked"))
				.build();
		case WALLET -> WalletDetails.builder().provider(d.getString("provider")).handle(d.getString("handle")).build();
		default -> null;
		};
	}
}
