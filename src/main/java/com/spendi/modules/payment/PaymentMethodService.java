/**
 * @file PaymentMethodService.java
 * @module modules/payment
 * 
 * @author Dmytro Shakh
 */

package com.spendi.modules.payment;

/**
 * ! lib imports
 */
import org.bson.types.ObjectId;
import java.time.Instant;
import java.util.Map;

import com.spendi.core.base.database.MongoUpdateBuilder;
/**
 * ! java imports
 */
import com.spendi.core.base.service.BaseRepositoryService;
import com.spendi.core.response.ServiceResponse;
import com.spendi.modules.payment.dto.PaymentMethodCreateDto;

public class PaymentMethodService extends BaseRepositoryService<PaymentMethodRepository, PaymentMethodEntity> {

	private static volatile PaymentMethodService INSTANCE;

	public PaymentMethodService(PaymentMethodRepository repository) {
		super(PaymentMethodService.class.getSimpleName(), repository);
	}

	public static void init(PaymentMethodRepository repository) {
		synchronized (PaymentMethodService.class) {
			if (INSTANCE == null) {
				INSTANCE = new PaymentMethodService(repository);
			}
		}
	}

	public static PaymentMethodService getInstance() {
		PaymentMethodService ref = INSTANCE;
		if (ref == null)
			throw new IllegalStateException("PaymentMethodService not initialized");
		return ref;
	}

	/**
	 * Создать способ оплаты для пользователя.
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param dto       данные для создания способа оплаты
	 * @return созданный способ оплаты
	 */
	public ServiceResponse<PaymentMethodEntity> createOne(String requestId, String userId, PaymentMethodCreateDto dto) {
		var now = Instant.now();

		// Assemble entity
		PaymentMethodEntity e = new PaymentMethodEntity();
		var info = new PaymentMethodEntity.Info();
		info.type = dto.info.type;
		info.name = dto.info.name;
		info.currency = dto.info.currency;
		info.order = dto.info.order;
		info.tags = dto.info.tags;
		e.info = info;

		var details = new PaymentMethodEntity.Details();
		if (dto.details != null) {
			if (dto.details.card != null) {
				var c = new PaymentMethodEntity.Card();
				c.brand = dto.details.card.brand;
				c.last4 = dto.details.card.last4;
				c.expMonth = dto.details.card.expMonth;
				c.expYear = dto.details.card.expYear;
				details.card = c;
			}
			if (dto.details.bank != null) {
				var b = new PaymentMethodEntity.Bank();
				b.bankName = dto.details.bank.bankName;
				b.accountMasked = dto.details.bank.accountMasked;
				details.bank = b;
			}
			if (dto.details.wallet != null) {
				var w = new PaymentMethodEntity.Wallet();
				w.provider = dto.details.wallet.provider;
				w.handle = dto.details.wallet.handle;
				details.wallet = w;
			}
		}
		e.details = details;

		var sys = new PaymentMethodEntity.System();
		sys.status = PaymentMethodEntity.EPaymentMethodStatus.Active;
		sys.meta = new PaymentMethodEntity.Meta();
		sys.meta.createdAt = now;
		sys.meta.updatedAt = null;
		sys.meta.archivedAt = null;
		e.system = sys;

		e.id = new ObjectId();
		e.userId = new ObjectId(userId);

		var created = super.createOne(e);

		return created;
	}

	/**
	 * Обновить поле order у метода оплаты.
	 * 
	 * @param requestId request-id для корреляции логов
	 * @param userId    строковый ObjectId пользователя
	 * @param methodId  строковый ObjectId способа оплаты
	 * @param order     новый порядок способа оплаты
	 * @return обновленный способ оплаты
	 */
	public ServiceResponse<PaymentMethodEntity> updateOrder(String requestId, String userId, String methodId,
			int order) {
		// ensure belongs to user
		var pm = this.getById(methodId).getData();
		if (pm.userId == null || !pm.userId.toHexString().equals(userId)) {
			// если не совпадает, просто отдаём not found
			throw new com.spendi.core.exceptions.EntityNotFoundException(PaymentMethodEntity.class.getSimpleName(),
					Map.of("id", methodId, "userId", userId));
		}

		var updateBuilder = new MongoUpdateBuilder();
		updateBuilder.set("info.order", order);
		updateBuilder.currentDate("system.meta.updatedAt");

		var updated = this.updateById(methodId, updateBuilder.build());

		return ServiceResponse.updated(updated.getData());
	}
}
