
/**
 * @file PaymentMethodMapper.java
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

/**
 * ! java imports
 */
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ! my imports
 */
import com.spendi.core.base.BaseMapper;
import com.spendi.core.types.DocMapper;
import com.spendi.modules.payment.cmd.PaymentMethodCreateCmd;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateBankDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateCardDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateDetailsDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateInfoDto;
import com.spendi.modules.payment.dto.create.PaymentMethodCreateWalletDto;
import com.spendi.modules.payment.model.BankDetails;
import com.spendi.modules.payment.model.CardDetails;
import com.spendi.modules.payment.model.EPaymentMethodStatus;
import com.spendi.modules.payment.model.EPaymentMethodType;
import com.spendi.modules.payment.model.PaymentMethodDetails;
import com.spendi.modules.payment.model.PaymentMethodEntity;
import com.spendi.modules.payment.model.PaymentMethodInfo;
import com.spendi.modules.payment.model.PaymentMethodSystem;
import com.spendi.modules.payment.model.WalletDetails;
import com.spendi.shared.mapper.meta.LifecycleMetaMapper;
import com.spendi.shared.model.meta.LifecycleMeta;

public class PaymentMethodMapper extends BaseMapper<PaymentMethodEntity> implements DocMapper<PaymentMethodEntity> {

	private final static PaymentMethodMapper mapper = new PaymentMethodMapper();
	private static final LifecycleMetaMapper META = LifecycleMetaMapper.getInstance();

	public PaymentMethodMapper() {
		super(PaymentMethodMapper.class.getSimpleName(), PaymentMethodEntity.class, "payment_methods");
	}

	public static PaymentMethodMapper getInstance() {
		return mapper;
	}

	/**
	 * ? === === === MAPPING === === ===
	 */

	@Override
	public PaymentMethodEntity toEntity(Document doc) {
		if (doc == null)
			return null;

		ObjectId id = reqObjectId(doc, "_id", null);
		ObjectId userId = reqObjectId(doc, "userId", id);

		// --- info ---
		Document infoDoc = reqSubDoc(doc, "info", id, "info");
		EPaymentMethodType type = reqEnum(infoDoc, "type", EPaymentMethodType.class, id);
		String name = reqString(infoDoc, "name", id, "info.name");
		String currency = reqString(infoDoc, "currency", id, "info.currency");
		Integer order = reqInt(infoDoc, "order", id, "info.order");

		// tags — допускаем отсутствие; если есть, приводим все элементы к String
		Set<String> tags = Set.of();
		Object rawTags = infoDoc.get("tags");
		if (rawTags instanceof List<?> list) {
			tags = list.stream().map(String::valueOf).collect(Collectors.toUnmodifiableSet());
		}

		var info = PaymentMethodInfo.builder().type(type).name(name).currency(currency).order(order).tags(tags).build();

		// --- details ---
		PaymentMethodDetails details = null;
		Document detDoc = doc.get("details", Document.class);
		if (detDoc != null) {
			switch (type) {
			case CARD -> details = CardDetails.builder().brand(reqString(detDoc, "brand", id, "details.brand"))
					.last4(reqString(detDoc, "last4", id, "details.last4"))
					.expMonth(reqInt(detDoc, "expMonth", id, "details.expMonth"))
					.expYear(reqInt(detDoc, "expYear", id, "details.expYear")).build();
			case BANK -> details = BankDetails.builder().bankName(reqString(detDoc, "bankName", id, "details.bankName"))
					.accountMasked(reqString(detDoc, "accountMasked", id, "details.accountMasked")).build();
			case WALLET -> details = WalletDetails.builder()
					.provider(reqString(detDoc, "provider", id, "details.provider"))
					.handle(reqString(detDoc, "handle", id, "details.handle")).build();
			}
		}

		// --- system ---
		Document sysDoc = reqSubDoc(doc, "system", id, "system");
		EPaymentMethodStatus status = reqEnum(sysDoc, "status", EPaymentMethodStatus.class, id, "system.status");
		Document metaDoc = reqSubDoc(sysDoc, "meta", id, "system.meta");
		LifecycleMeta meta = META.toEntity(metaDoc);

		var system = PaymentMethodSystem.builder().status(status).meta(meta).build();

		return PaymentMethodEntity.builder().id(id).userId(userId).info(info).details(details).system(system).build();

	}

	@Override
	public Document toDocument(PaymentMethodEntity e) {
		if (e == null)
			return new Document();

		// обязательные поля строго проверяем
		if (e.getId() == null)
			missing("ObjectId", "_id", null);
		if (e.getUserId() == null)
			missing("ObjectId", "userId", e.getId());
		if (e.getInfo() == null)
			missing("Object", "info", e.getId());
		if (e.getSystem() == null)
			missing("Object", "system", e.getId());

		PaymentMethodInfo i = e.getInfo();
		if (i.getType() == null)
			missing("Enum", "info.type", e.getId());
		if (i.getName() == null)
			missing("String", "info.name", e.getId());
		if (i.getCurrency() == null)
			missing("String", "info.currency", e.getId());
		// order и tags допустимы как есть (order — int, tags — Set<String> может быть пустым)

		Document doc = new Document();
		doc.put("_id", e.getId());
		doc.put("userId", e.getUserId());

		// info
		Document di = new Document();
		di.put("type", i.getType().name());
		di.put("name", i.getName());
		di.put("currency", i.getCurrency());
		di.put("order", i.getOrder());
		di.put("tags", i.getTags() == null ? List.of() : List.copyOf(i.getTags()));
		doc.put("info", di);

		// details (если есть)
		var det = e.getDetails();
		if (det instanceof CardDetails c) {
			Document dd = new Document();
			dd.put("kind", "CARD");
			dd.put("brand", c.getBrand());
			dd.put("last4", c.getLast4());
			dd.put("expMonth", c.getExpMonth());
			dd.put("expYear", c.getExpYear());
			doc.put("details", dd);
		} else if (det instanceof BankDetails b) {
			Document dd = new Document();
			dd.put("kind", "BANK");
			dd.put("bankName", b.getBankName());
			dd.put("accountMasked", b.getAccountMasked());
			doc.put("details", dd);
		} else if (det instanceof WalletDetails w) {
			Document dd = new Document();
			dd.put("kind", "WALLET");
			dd.put("provider", w.getProvider());
			dd.put("handle", w.getHandle());
			doc.put("details", dd);
		}
		// если details == null — не пишем поле вовсе

		// system
		var s = e.getSystem();
		if (s.getStatus() == null)
			missing("Enum", "system.status", e.getId());
		if (s.getMeta() == null)
			missing("Object", "system.meta", e.getId());

		Document ds = new Document();
		ds.put("status", s.getStatus().name());
		ds.put("meta", META.toDocument(s.getMeta()));
		doc.put("system", ds);

		return doc;
	}

	/**
	 * Преобразовать DTO в команду создания метода оплаты. Предполагается, что ConsistentPaymentMethodValidator уже
	 * проверил соответствие type ↔ details.
	 */
	public PaymentMethodCreateCmd toCmd(PaymentMethodCreateDto dto) {
		if (dto == null) {
			throw new IllegalArgumentException("PaymentMethodCreateDto must not be null");
		}

		// Преобразуем info
		PaymentMethodCreateInfoDto infoDto = dto.getInfo();
		PaymentMethodInfo info = PaymentMethodInfo.builder().type(infoDto.getType()).name(infoDto.getName())
				.currency(infoDto.getCurrency()).order(infoDto.getOrder())
				.tags(infoDto.getTags() == null ? Set.of() : Set.copyOf(infoDto.getTags())).build();

		// Преобразуем details
		PaymentMethodDetails details = null;
		PaymentMethodCreateDetailsDto detailsDto = dto.getDetails();
		if (detailsDto != null) {
			if (detailsDto.getCard() != null) {
				PaymentMethodCreateCardDto c = detailsDto.getCard();
				details = CardDetails.builder().brand(c.getBrand()).last4(c.getLast4()).expMonth(c.getExpMonth())
						.expYear(c.getExpYear()).build();
			} else if (detailsDto.getBank() != null) {
				PaymentMethodCreateBankDto b = detailsDto.getBank();
				details = BankDetails.builder().bankName(b.getBankName()).accountMasked(b.getAccountMasked()).build();
			} else if (detailsDto.getWallet() != null) {
				PaymentMethodCreateWalletDto w = detailsDto.getWallet();
				details = WalletDetails.builder().provider(w.getProvider()).handle(w.getHandle()).build();
			}
		}

		return PaymentMethodCreateCmd.builder().info(info).details(details).build();
	}
}
